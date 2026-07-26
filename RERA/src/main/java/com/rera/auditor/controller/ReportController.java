package com.rera.auditor.controller;

import com.rera.auditor.dto.AuditReportResponse;
import com.rera.auditor.dto.GenerateReportRequest;
import com.rera.auditor.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<AuditReportResponse> generate(@PathVariable Long projectId,
                                                          @Valid @RequestBody GenerateReportRequest request) {
        return ResponseEntity.ok(reportService.generate(projectId, request.format()));
    }

    @GetMapping
    public ResponseEntity<List<AuditReportResponse>> list(@PathVariable Long projectId) {
        return ResponseEntity.ok(reportService.listForProject(projectId));
    }

    @GetMapping("/{reportId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long projectId, @PathVariable Long reportId) {
        var file = reportService.download(reportId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(file.filename()).build().toString())
            .contentType(org.springframework.http.MediaType.parseMediaType(file.contentType()))
            .body(file.resource());
    }
}
