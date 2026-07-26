package com.rera.auditor.dto;

import java.math.BigDecimal;

public record RoomResponse(
    Long id,
    String roomType,
    BigDecimal areaSqm,
    BigDecimal widthM,
    BigDecimal lengthM,
    Boolean hasNaturalLight,
    Boolean hasVentilation
) {}
