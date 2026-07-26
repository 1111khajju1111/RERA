package com.rera.auditor.repository;

import com.rera.auditor.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByProjectIdOrderByCreatedAtAsc(Long projectId);
}
