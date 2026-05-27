package com.smartcare.service;

import com.smartcare.model.entity.Message;
import com.smartcare.repository.MessageRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    @Transactional
    public Message sendMessage(UUID conversationId, Long senderId, String senderType,
                               String messageType, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setSenderType(senderType);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getConversationMessages(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);
    }

    public Page<Message> getConversationMessages(UUID conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
    }

    public List<Message> getMessagesBefore(UUID conversationId, LocalDateTime before, int limit) {
        return messageRepository.findByConversationIdBefore(conversationId, before,
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }
}