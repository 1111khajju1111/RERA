package com.rera.auditor.service;

import com.rera.auditor.config.CurrentUserService;
import com.rera.auditor.dto.ChatMessageResponse;
import com.rera.auditor.entity.ChatMessage;
import com.rera.auditor.entity.Project;
import com.rera.auditor.entity.User;
import com.rera.auditor.mapper.ChatMessageMapper;
import com.rera.auditor.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final ProjectService projectService;
    private final CurrentUserService currentUserService;
    private final AiServiceClient aiServiceClient;

    public ChatService(ChatMessageRepository chatMessageRepository, ChatMessageMapper chatMessageMapper,
                        ProjectService projectService, CurrentUserService currentUserService,
                        AiServiceClient aiServiceClient) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatMessageMapper = chatMessageMapper;
        this.projectService = projectService;
        this.currentUserService = currentUserService;
        this.aiServiceClient = aiServiceClient;
    }

    public List<ChatMessageResponse> getHistory(Long projectId) {
        return chatMessageRepository.findByProjectIdOrderByCreatedAtAsc(projectId)
            .stream().map(chatMessageMapper::toResponse).toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long projectId, String userMessage) {
        Project project = projectService.findProjectOrThrow(projectId);
        User user = currentUserService.getCurrentUser();

        ChatMessage userMsg = new ChatMessage();
        userMsg.setProject(project);
        userMsg.setUser(user);
        userMsg.setRole("USER");
        userMsg.setMessage(userMessage);
        chatMessageRepository.save(userMsg);

        String reply = aiServiceClient.chat(projectId, userMessage);

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setProject(project);
        assistantMsg.setRole("ASSISTANT");
        assistantMsg.setMessage(reply);
        ChatMessage saved = chatMessageRepository.save(assistantMsg);

        return chatMessageMapper.toResponse(saved);
    }
}
