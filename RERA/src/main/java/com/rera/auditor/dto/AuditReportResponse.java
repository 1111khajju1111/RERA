package com.rera.auditor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuditReportResponse(
    Long id,
    BigDecimal complianceScore,
    BigDecimal approvalProbability,
    String format,
    boolean downloadable,
    LocalDateTime generatedAt
) {}
