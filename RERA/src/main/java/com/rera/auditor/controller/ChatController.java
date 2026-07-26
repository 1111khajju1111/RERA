package com.rera.auditor.controller;

import com.rera.auditor.dto.ChatMessageResponse;
import com.rera.auditor.dto.ChatRequest;
import com.rera.auditor.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<ChatMessageResponse>> history(@PathVariable Long projectId) {
        return ResponseEntity.ok(chatService.getHistory(projectId));
    }

    @PostMapping
    public ResponseEntity<ChatMessageResponse> send(@PathVariable Long projectId, @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(projectId, request.message()));
    }
}
