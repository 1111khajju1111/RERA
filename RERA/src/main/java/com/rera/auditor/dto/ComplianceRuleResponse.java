package com.rera.auditor.dto;

import java.math.BigDecimal;

public record ComplianceRuleResponse(
    Long id,
    String ruleCode,
    String category,
    String description,
    String parameter,
    String operator,
    BigDecimal thresholdValue,
    String unit,
    String applicableBuildingType,
    String sourceReference,
    String defaultSeverity
) {}
