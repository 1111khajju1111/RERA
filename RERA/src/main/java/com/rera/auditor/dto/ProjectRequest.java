package com.rera.auditor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProjectRequest(
    @NotBlank @Size(max = 200) String name,
    String description,
    String location,
    BigDecimal plotAreaSqm
) {}
