package com.rera.auditor.service;

import com.rera.auditor.dto.AuditReportResponse;
import com.rera.auditor.entity.AuditReport;
import com.rera.auditor.exception.ResourceNotFoundException;
import com.rera.auditor.mapper.AuditReportMapper;
import com.rera.auditor.repository.AuditReportRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

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
     * Proxies the file's bytes from ai-service rather than reading local
     * disk — the backend and ai-service are two separate Render services
     * with separate filesystems, so a file_path written by ai-service is
     * never readable here directly. See AiServiceClient.downloadReportFile
     * and the matching /reports/{id}/file endpoint on the ai-service side.
     */
    public ReportFile download(Long reportId) {
        AuditReport report = auditReportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        if (report.getFilePath() == null) {
            throw new ResourceNotFoundException("This report has no downloadable file (it's a score snapshot, not a document)");
        }

        byte[] bytes = aiServiceClient.downloadReportFile(reportId);
        if (bytes == null || bytes.length == 0) {
            throw new ResourceNotFoundException("Report file could not be retrieved from the AI service — it may need to be regenerated");
        }

        String contentType = switch (report.getFormat()) {
            case "PDF" -> "application/pdf";
            case "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "XLSX" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default -> "application/octet-stream";
        };

        String extension = switch (report.getFormat()) {
            case "PDF" -> "pdf";
            case "DOCX" -> "docx";
            case "XLSX" -> "xlsx";
            default -> "bin";
        };
        String filename = "report_" + reportId + "." + extension;

        return new ReportFile(new ByteArrayResource(bytes), filename, contentType);
    }

    public record ReportFile(Resource resource, String filename, String contentType) {}
}
