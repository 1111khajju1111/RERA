package com.rera.auditor.dto;

import jakarta.validation.constraints.NotBlank;

public record GisAnalyzeRequest(
    @NotBlank String address
) {}
