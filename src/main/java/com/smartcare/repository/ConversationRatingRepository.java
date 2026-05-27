package com.smartcare.repository;

import com.smartcare.model.entity.ConversationRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRatingRepository extends JpaRepository<ConversationRating, Long> {

    Optional<ConversationRating> findByConversationId(UUID conversationId);

    boolean existsByConversationId(UUID conversationId);
}