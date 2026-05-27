package com.multica.customer.service;

import com.multica.customer.entity.Agent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 坐席服务 - 坐席状态管理、最少连接数分配算法
 */
@Slf4j
@Service
public class AgentService {

    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    private final Map<String, String> agentSessions = new ConcurrentHashMap<>(); // agentId -> sessionId

    public AgentService() {
        // 初始化坐席数据（实际应从数据库加载）
        initAgents();
    }

    private void initAgents() {
        for (int i = 1; i <= 3; i++) {
            String agentId = "agent_" + i;
            Agent agent = Agent.builder()
                    .agentId(agentId)
                    .name("坐席" + i)
                    .status(Agent.AgentStatus.ONLINE)
                    .skills("general")
                    .currentLoad(0)
                    .maxLoad(10)
                    .lastActiveAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();
            agents.put(agentId, agent);
        }
        log.info("初始化 {} 个坐席", agents.size());
    }

    /**
     * 查找可用坐席 - 最少连接数算法
     */
    public Agent findAvailableAgent() {
        return agents.values().stream()
                .filter(a -> a.getStatus() == Agent.AgentStatus.ONLINE)
                .filter(a -> a.getCurrentLoad() < a.getMaxLoad())
                .min((a1, a2) -> Integer.compare(a1.getCurrentLoad(), a2.getCurrentLoad()))
                .orElse(null);
    }

    /**
     * 分配会话给坐席
     */
    public void assignSession(String agentId, String sessionId) {
        Agent agent = agents.get(agentId);
        if (agent == null) {
            log.warn("坐席不存在: {}", agentId);
            return;
        }

        agent.setCurrentLoad(agent.getCurrentLoad() + 1);
        agent.setStatus(Agent.AgentStatus.BUSY);
        agent.setLastActiveAt(LocalDateTime.now());
        agentSessions.put(agentId, sessionId);

        log.info("坐席 {} 承接会话 {}, 当前负载: {}", agentId, sessionId, agent.getCurrentLoad());
    }

    /**
     * 释放坐席
     */
    public void releaseAgent(String agentId) {
        Agent agent = agents.get(agentId);
        if (agent == null) {
            return;
        }

        agent.setCurrentLoad(Math.max(0, agent.getCurrentLoad() - 1));
        if (agent.getCurrentLoad() == 0) {
            agent.setStatus(Agent.AgentStatus.ONLINE);
        }
        agent.setLastActiveAt(LocalDateTime.now());
        agentSessions.remove(agentId);

        log.info("坐席 {} 释放会话, 当前负载: {}", agentId, agent.getCurrentLoad());
    }

    /**
     * 获取坐席
     */
    public Agent getAgent(String agentId) {
        return agents.get(agentId);
    }

    /**
     * 更新坐席状态
     */
    public void updateAgentStatus(String agentId, Agent.AgentStatus status) {
        Agent agent = agents.get(agentId);
        if (agent != null) {
            agent.setStatus(status);
            agent.setLastActiveAt(LocalDateTime.now());
            log.info("坐席 {} 状态更新为: {}", agentId, status);
        }
    }

    /**
     * 获取所有在线坐席
     */
    public Map<String, Agent> getOnlineAgents() {
        return agents;
    }
}
