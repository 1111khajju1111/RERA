package com.rera.auditor.mapper;

import com.rera.auditor.dto.AuditReportResponse;
import com.rera.auditor.entity.AuditReport;
import org.springframework.stereotype.Component;

@Component
public class AuditReportMapper {
    public AuditReportResponse toResponse(AuditReport r) {
        // "SNAPSHOT" rows (see AI service scoring.py) are score-only, written
        // after every rule-engine or GIS run — they have no file. Only rows
        // written by the actual report generator (PDF/DOCX/XLSX) have a
        // file_path and are downloadable.
        boolean downloadable = r.getFilePath() != null && !r.getFilePath().isBlank();
        return new AuditReportResponse(
            r.getId(), r.getComplianceScore(), r.getApprovalProbability(),
            r.getFormat(), downloadable, r.getGeneratedAt()
        );
    }
}
