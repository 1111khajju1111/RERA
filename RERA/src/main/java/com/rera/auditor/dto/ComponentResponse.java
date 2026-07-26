package com.rera.auditor.dto;

import java.math.BigDecimal;

public record ComponentResponse(
    Long id,
    String componentType,
    String geometryJson,
    BigDecimal posX,
    BigDecimal posY,
    BigDecimal width,
    BigDecimal height,
    String material,
    BigDecimal confidenceScore,
    String detectedBy
) {}
