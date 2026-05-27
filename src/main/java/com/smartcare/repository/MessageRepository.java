package com.smartcare.repository;

import com.smartcare.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    Page<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.createdAt < :before ORDER BY m.createdAt DESC")
    List<Message> findByConversationIdBefore(@Param("conversationId") UUID conversationId, @Param("before") java.time.LocalDateTime before, Pageable pageable);
}