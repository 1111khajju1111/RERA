package com.rera.auditor.dto;

import java.math.BigDecimal;
import java.util.List;

public record FloorResponse(
    Long id,
    Integer floorNumber,
    BigDecimal floorHeightM,
    BigDecimal floorAreaSqm,
    List<RoomResponse> rooms,
    List<ComponentResponse> components
) {}
