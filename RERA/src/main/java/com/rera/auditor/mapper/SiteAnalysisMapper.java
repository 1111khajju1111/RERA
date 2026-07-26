package com.rera.auditor.mapper;

import com.rera.auditor.dto.SiteAnalysisResponse;
import com.rera.auditor.entity.SiteAnalysis;
import org.springframework.stereotype.Component;

@Component
public class SiteAnalysisMapper {
    public SiteAnalysisResponse toResponse(SiteAnalysis s) {
        return new SiteAnalysisResponse(
            s.getLatitude(), s.getLongitude(), s.getGeocodedAddress(),
            s.getNearestRoadDistanceM(), s.getNearestRoadWidthM(), s.getNearestRoadWidthIsEstimated(),
            s.getNearestRoadType(), s.getNearestRoadName(),
            s.getFireAccessCompliant(),
            s.getEncroachmentStatus(), s.getEncroachmentNotes(),
            s.getNearbyRoadsGeojson(), s.getAnalyzedAt()
        );
    }
}
