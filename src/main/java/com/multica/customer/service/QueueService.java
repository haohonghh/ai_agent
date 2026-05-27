package com.multica.customer.service;

import com.multica.customer.entity.QueueEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 队列服务 - 排队管理、FIFO分配、超时处理
 */
@Slf4j
@Service
public class QueueService {

    private final PriorityBlockingQueue<QueueEntry> waitQueue = new PriorityBlockingQueue<>(
            100,
            Comparator.comparingInt((QueueEntry e) -> e.getPriority().getLevel())
                    .thenComparing(QueueEntry::getEnqueuedAt)
    );

    private final Map<String, String> sessionQueueMap = new ConcurrentHashMap<>(); // sessionId -> queueId
    private final Map<String, QueueEntry> queueEntries = new ConcurrentHashMap<>();

    private final SessionService sessionService;

    public QueueService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * 入队
     */
    public int enqueue(String sessionId) {
        String queueId = UUID.randomUUID().toString();

        QueueEntry entry = QueueEntry.builder()
                .queueId(queueId)
                .sessionId(sessionId)
                .position(waitQueue.size() + 1)
                .priority(QueueEntry.QueuePriority.NORMAL)
                .enqueuedAt(LocalDateTime.now())
                .timeoutAt(LocalDateTime.now().plusMinutes(10))
                .build();

        waitQueue.offer(entry);
        queueEntries.put(queueId, entry);
        sessionQueueMap.put(sessionId, queueId);

        log.info("会话 {} 入队, 位置: {}", sessionId, entry.getPosition());
        return entry.getPosition();
    }

    /**
     * 出队 - 获取下一个
     */
    public Optional<QueueEntry> dequeue() {
        QueueEntry entry = waitQueue.poll();
        if (entry == null) {
            return Optional.empty();
        }

        sessionQueueMap.remove(entry.getSessionId());
        queueEntries.remove(entry.getQueueId());

        // 更新队列中其他人的位置
        reindexQueue();

        log.info("会话 {} 出队", entry.getSessionId());
        return Optional.of(entry);
    }

    /**
     * 根据会话ID出队
     */
    public void dequeue(String sessionId) {
        String queueId = sessionQueueMap.remove(sessionId);
        if (queueId == null) {
            return;
        }

        QueueEntry entry = queueEntries.remove(queueId);
        if (entry != null) {
            waitQueue.remove(entry);
            reindexQueue();
            log.info("会话 {} 取消排队", sessionId);
        }
    }

    /**
     * 获取用户在队列中的位置
     */
    public int getQueuePosition(String sessionId) {
        String queueId = sessionQueueMap.get(sessionId);
        if (queueId == null) {
            return -1;
        }

        QueueEntry entry = queueEntries.get(queueId);
        if (entry == null) {
            return -1;
        }

        int position = 1;
        for (QueueEntry e : waitQueue) {
            if (e.getSessionId().equals(sessionId)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    /**
     * 获取当前排队人数
     */
    public int getQueueSize() {
        return waitQueue.size();
    }

    /**
     * 定时任务：处理超时
     */
    @Scheduled(fixedRate = 30000)
    public void processTimeout() {
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, QueueEntry> entry : queueEntries.entrySet()) {
            QueueEntry qe = entry.getValue();
            if (qe.getTimeoutAt().isBefore(now)) {
                log.warn("排队超时: sessionId={}, queueId={}", qe.getSessionId(), entry.getKey());
                sessionService.closeSession(qe.getSessionId());
            }
        }
    }

    /**
     * 重新索引队列位置
     */
    private void reindexQueue() {
        int position = 1;
        for (QueueEntry entry : waitQueue) {
            entry.setPosition(position++);
        }
    }
}
