package com.rera.auditor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComplianceHistoryEntry(
    BigDecimal complianceScore,
    BigDecimal approvalProbability,
    LocalDateTime generatedAt
) {}
