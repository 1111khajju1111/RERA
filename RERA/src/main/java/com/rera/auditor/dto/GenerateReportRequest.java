package com.rera.auditor.dto;

import jakarta.validation.constraints.Pattern;

public record GenerateReportRequest(
    @Pattern(regexp = "PDF|DOCX|XLSX", message = "format must be PDF, DOCX, or XLSX") String format
) {}
