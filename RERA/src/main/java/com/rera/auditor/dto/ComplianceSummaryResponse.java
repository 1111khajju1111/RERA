package com.rera.auditor.dto;

import java.math.BigDecimal;

public record ComplianceSummaryResponse(
    BigDecimal complianceScore,
    BigDecimal approvalProbability,
    long totalViolations,
    long criticalViolations,
    long majorViolations,
    long minorViolations
) {}
