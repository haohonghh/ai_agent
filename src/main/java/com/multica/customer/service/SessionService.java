package com.multica.customer.service;

import com.multica.customer.dto.CreateSessionRequest;
import com.multica.customer.dto.SessionResponse;
import com.multica.customer.entity.Agent;
import com.multica.customer.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话服务 - 负责会话创建、状态流转
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final AgentService agentService;
    private final QueueService queueService;

    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();

    /**
     * 创建新会话
     */
    public SessionResponse createSession(CreateSessionRequest request) {
        String sessionId = UUID.randomUUID().toString();

        ChatSession session = ChatSession.builder()
                .sessionId(sessionId)
                .userId(request.getUserId())
                .userName(request.getUserName())
                .source(parseSource(request.getSource()))
                .status(ChatSession.SessionStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();

        sessions.put(sessionId, session);
        log.info("创建新会话: {}, 用户: {}", sessionId, request.getUserId());

        // 尝试分配坐席
        return assignAgent(session);
    }

    /**
     * 分配坐席
     */
    public SessionResponse assignAgent(ChatSession session) {
        String sessionId = session.getSessionId();

        // 尝试获取空闲坐席
        Agent agent = agentService.findAvailableAgent();

        if (agent != null) {
            // 分配成功
            session.setAgentId(agent.getAgentId());
            session.setStatus(ChatSession.SessionStatus.CONNECTED);
            session.setConnectedAt(LocalDateTime.now());

            if (session.getWaitTimeSeconds() == null) {
                session.setWaitTimeSeconds(
                        java.time.Duration.between(session.getCreatedAt(), LocalDateTime.now()).getSeconds()
                );
            }

            agentService.assignSession(agent.getAgentId(), sessionId);
            log.info("会话 {} 分配给坐席 {}", sessionId, agent.getAgentId());

            return SessionResponse.connected(sessionId, agent.getAgentId(), agent.getName());
        } else {
            // 无空闲坐席，进入排队
            int position = queueService.enqueue(sessionId);
            session.setStatus(ChatSession.SessionStatus.QUEUED);

            log.info("会话 {} 进入排队，位置: {}", sessionId, position);

            return SessionResponse.queued(sessionId, position, 60L);
        }
    }

    /**
     * 根据会话ID获取会话
     */
    public ChatSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * 结束会话
     */
    public void closeSession(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        if (session == null) {
            return;
        }

        session.setStatus(ChatSession.SessionStatus.CLOSED);
        session.setEndedAt(LocalDateTime.now());

        if (session.getAgentId() != null) {
            agentService.releaseAgent(session.getAgentId());
        }

        queueService.dequeue(sessionId);
        log.info("会话已关闭: {}", sessionId);
    }

    /**
     * 转接会话
     */
    public SessionResponse transferSession(String sessionId, String targetAgentId) {
        ChatSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalStateException("会话不存在: " + sessionId);
        }

        String oldAgentId = session.getAgentId();
        session.setAgentId(targetAgentId);
        session.setStatus(ChatSession.SessionStatus.TRANSFERRED);

        if (oldAgentId != null) {
            agentService.releaseAgent(oldAgentId);
        }

        Agent targetAgent = agentService.getAgent(targetAgentId);
        log.info("会话 {} 从 {} 转接到 {}", sessionId, oldAgentId, targetAgentId);

        return SessionResponse.connected(sessionId, targetAgentId, targetAgent.getName());
    }

    private ChatSession.SessionSource parseSource(String source) {
        if (source == null) {
            return ChatSession.SessionSource.WEB;
        }
        try {
            return ChatSession.SessionSource.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChatSession.SessionSource.WEB;
        }
    }
}
