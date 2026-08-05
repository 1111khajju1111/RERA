package com.rera.auditor.service;

import com.rera.auditor.dto.ProjectVersionResponse;
import com.rera.auditor.entity.ProjectVersion;
import com.rera.auditor.exception.ResourceNotFoundException;
import com.rera.auditor.repository.AuditReportRepository;
import com.rera.auditor.repository.ProjectVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * "Project Timeline" — enriches each uploaded version with the compliance
 * score snapshot it produced (if analyzed).
 *
 * Scope note: this does NOT version the building/floor/room/component
 * geometry itself — only the uploaded file + the score it produced.
 * "Rollback" here means re-analyzing an old file (see UploadService.
 * reanalyzeVersion), not restoring historical geometry as the project's
 * current live state. True state rollback would need per-version
 * geometry snapshots — a larger schema change, deferred for now.
 */
@Service
public class ProjectVersionService {

    private final ProjectVersionRepository projectVersionRepository;
    private final AuditReportRepository auditReportRepository;
    private final UploadService uploadService;

    public ProjectVersionService(ProjectVersionRepository projectVersionRepository,
                                  AuditReportRepository auditReportRepository,
                                  UploadService uploadService) {
        this.projectVersionRepository = projectVersionRepository;
        this.auditReportRepository = auditReportRepository;
        this.uploadService = uploadService;
    }

    @Transactional(readOnly = true)
    public List<ProjectVersionResponse> listForProject(Long projectId) {
        return projectVersionRepository.findByProjectIdOrderByVersionNumberDesc(projectId)
            .stream()
            .map(this::toEnrichedResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public void reanalyze(Long projectId, Long versionId) {
        ProjectVersion version = projectVersionRepository.findById(versionId)
            .filter(v -> v.getProject().getId().equals(projectId))
            .orElseThrow(() -> new ResourceNotFoundException(
                "Version " + versionId + " not found for project " + projectId));
        uploadService.reanalyzeVersion(projectId, version);
    }

    private ProjectVersionResponse toEnrichedResponse(ProjectVersion version) {
        var scoreSnapshot = auditReportRepository.findTopByProjectVersionIdOrderByGeneratedAtDesc(version.getId());
        return new ProjectVersionResponse(
            version.getId(),
            version.getVersionNumber(),
            version.getOriginalFilename(),
            version.getFileType(),
            version.getNotes(),
            version.getUploadedAt(),
            scoreSnapshot.map(r -> r.getComplianceScore()).orElse(null),
            scoreSnapshot.map(r -> r.getApprovalProbability()).orElse(null)
        );
    }
}
