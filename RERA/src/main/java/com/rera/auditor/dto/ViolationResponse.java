package com.rera.auditor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ViolationResponse(
    Long id,
    String ruleCode,
    String category,
    String severity,
    String description,
    BigDecimal detectedValue,
    BigDecimal requiredValue,
    String unit,
    String status,
    Integer floorNumber,
    String componentType,
    Long componentId,
    String resolutionNote,
    LocalDateTime detectedAt
) {}
