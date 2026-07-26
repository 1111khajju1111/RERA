package com.rera.auditor.mapper;

import com.rera.auditor.dto.ChatMessageResponse;
import com.rera.auditor.entity.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {
    public ChatMessageResponse toResponse(ChatMessage m) {
        return new ChatMessageResponse(m.getId(), m.getRole(), m.getMessage(), m.getCreatedAt());
    }
}
