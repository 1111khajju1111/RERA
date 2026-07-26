package com.rera.auditor.dto;

public record AuthResponse(
    UserResponse user,
    String token
) {}
