package com.rera.auditor.dto;

import java.math.BigDecimal;
import java.util.List;

public record BuildingResponse(
    Long id,
    String name,
    String buildingType,
    Integer numFloors,
    BigDecimal heightM,
    BigDecimal builtUpAreaSqm,
    BigDecimal farCalculated,
    BigDecimal groundCoveragePct,
    List<FloorResponse> floors
) {}
