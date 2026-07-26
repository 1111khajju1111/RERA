package com.rera.auditor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    String location,
    BigDecimal plotAreaSqm,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
