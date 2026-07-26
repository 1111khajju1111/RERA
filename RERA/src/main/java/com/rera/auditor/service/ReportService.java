package com.rera.auditor.service;

import com.rera.auditor.dto.AuditReportResponse;
import com.rera.auditor.entity.AuditReport;
import com.rera.auditor.exception.ResourceNotFoundException;
import com.rera.auditor.mapper.AuditReportMapper;
import com.rera.auditor.repository.AuditReportRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class ReportService {

    private final AiServiceClient aiServiceClient;
    private final AuditReportRepository auditReportRepository;
    private final ProjectService projectService;
    private final AuditReportMapper mapper;

    public ReportService(AiServiceClient aiServiceClient, AuditReportRepository auditReportRepository,
                          ProjectService projectService, AuditReportMapper mapper) {
        this.aiServiceClient = aiServiceClient;
        this.auditReportRepository = auditReportRepository;
        this.projectService = projectService;
        this.mapper = mapper;
    }

    /** Synchronous — generating a PDF/DOCX/XLSX from already-computed data takes well under a second. */
    public AuditReportResponse generate(Long projectId, String format) {
        projectService.findProjectOrThrow(projectId);
        aiServiceClient.generateReport(projectId, format);
        return listForProject(projectId).stream()
            .filter(r -> r.downloadable())
            .findFirst() // most recent, since the repository query orders by generatedAt desc
            .orElseThrow(() -> new ResourceNotFoundException("Report generation did not produce a downloadable file"));
    }

    public List<AuditReportResponse> listForProject(Long projectId) {
        return auditReportRepository.findByProjectIdOrderByGeneratedAtDesc(projectId)
            .stream().map(mapper::toResponse).toList();
    }

    /**
     * Streams the file straight from disk. Requires the backend and AI
     * service to share the same filesystem path for reports (see Phase 10
     * docker-compose's shared `reports_data` volume) — this does NOT proxy
     * bytes over HTTP from the AI service, it reads the file the AI service
     * already wrote to a mounted volume both containers can see.
     */
    public ReportFile download(Long reportId) {
        AuditReport report = auditReportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        if (report.getFilePath() == null) {
            throw new ResourceNotFoundException("This report has no downloadable file (it's a score snapshot, not a document)");
        }

        File file = new File(report.getFilePath());
        if (!file.exists()) {
            throw new ResourceNotFoundException(
                "Report file is missing on disk at " + report.getFilePath() +
                " — check that the backend and AI service share the reports volume"
            );
        }

        String contentType = switch (report.getFormat()) {
            case "PDF" -> "application/pdf";
            case "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "XLSX" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default -> "application/octet-stream";
        };

        return new ReportFile(new FileSystemResource(file), file.getName(), contentType);
    }

    public record ReportFile(Resource resource, String filename, String contentType) {}
}
