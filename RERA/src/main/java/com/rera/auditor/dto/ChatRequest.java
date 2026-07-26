package com.rera.auditor.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank String message
) {}
