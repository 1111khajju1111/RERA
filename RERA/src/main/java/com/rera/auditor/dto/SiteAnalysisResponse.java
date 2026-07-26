package com.rera.auditor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SiteAnalysisResponse(
    BigDecimal latitude,
    BigDecimal longitude,
    String geocodedAddress,
    BigDecimal nearestRoadDistanceM,
    BigDecimal nearestRoadWidthM,
    Boolean nearestRoadWidthIsEstimated,
    String nearestRoadType,
    String nearestRoadName,
    Boolean fireAccessCompliant,
    String encroachmentStatus,
    String encroachmentNotes,
    String nearbyRoadsGeojson,
    LocalDateTime analyzedAt
) {}
