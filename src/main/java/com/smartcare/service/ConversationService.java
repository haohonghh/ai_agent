package com.smartcare.service;

import com.smartcare.model.entity.Conversation;
import com.smartcare.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    @Transactional
    public Conversation createConversation(String source, Integer priority, Long customerId) {
        Conversation conversation = new Conversation();
        conversation.setCustomerId(customerId);
        conversation.setSource(source != null ? source : "web");
        conversation.setPriority(priority != null ? priority : 0);
        conversation.setStatus("waiting");
        conversation.setCreatedAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }

    public Optional<Conversation> getConversationById(UUID id) {
        return conversationRepository.findById(id);
    }

    public Page<Conversation> getConversations(String status, Long agentId, Pageable pageable) {
        if (status != null && agentId != null) {
            return conversationRepository.findByAgentIdAndStatus(agentId, status).stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> new PageImpl<>(list, pageable, list.size())
                    ));
        } else if (status != null) {
            return conversationRepository.findByStatus(status, pageable);
        } else if (agentId != null) {
            return conversationRepository.findByAgentId(agentId, pageable);
        }
        return conversationRepository.findAll(pageable);
    }

    public List<Conversation> getWaitingConversations() {
        return conversationRepository.findWaitingConversations("waiting");
    }

    @Transactional
    public Optional<Conversation> assignAgent(UUID conversationId, Long agentId) {
        return conversationRepository.findById(conversationId).map(conv -> {
            conv.setAgentId(agentId);
            conv.setStatus("active");
            return conversationRepository.save(conv);
        });
    }

    @Transactional
    public Optional<Conversation> closeConversation(UUID conversationId) {
        return conversationRepository.findById(conversationId).map(conv -> {
            conv.setStatus("closed");
            conv.setClosedAt(LocalDateTime.now());
            return conversationRepository.save(conv);
        });
    }
}