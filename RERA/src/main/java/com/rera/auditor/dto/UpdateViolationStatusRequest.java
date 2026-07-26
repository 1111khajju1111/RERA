package com.rera.auditor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateViolationStatusRequest(
    @NotBlank @Pattern(regexp = "OPEN|RESOLVED|WAIVED", message = "status must be OPEN, RESOLVED, or WAIVED")
    String status,
    String note
) {}
