package com.rera.auditor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProjectVersionResponse(
    Long id,
    Integer versionNumber,
    String originalFilename,
    String fileType,
    String notes,
    LocalDateTime uploadedAt,
    BigDecimal complianceScore,
    BigDecimal approvalProbability
) {}
