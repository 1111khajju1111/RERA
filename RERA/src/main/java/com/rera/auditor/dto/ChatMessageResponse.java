package com.rera.auditor.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long id,
    String role,
    String message,
    LocalDateTime createdAt
) {}
