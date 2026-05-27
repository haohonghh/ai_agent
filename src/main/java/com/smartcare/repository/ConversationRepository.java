package com.smartcare.repository;

import com.smartcare.model.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Page<Conversation> findByStatus(String status, Pageable pageable);

    Page<Conversation> findByAgentId(Long agentId, Pageable pageable);

    @Query("SELECT c FROM Conversation c WHERE c.status = :status AND c.agentId IS NULL ORDER BY c.priority DESC, c.createdAt ASC")
    List<Conversation> findWaitingConversations(@Param("status") String status);

    @Query("SELECT c FROM Conversation c WHERE c.agentId = :agentId AND c.status = :status")
    List<Conversation> findByAgentIdAndStatus(@Param("agentId") Long agentId, @Param("status") String status);
}