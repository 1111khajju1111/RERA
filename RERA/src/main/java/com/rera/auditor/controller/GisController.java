package com.rera.auditor.controller;

import com.rera.auditor.dto.GisAnalyzeRequest;
import com.rera.auditor.dto.SiteAnalysisResponse;
import com.rera.auditor.service.GisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/gis")
public class GisController {

    private final GisService gisService;

    public GisController(GisService gisService) {
        this.gisService = gisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<SiteAnalysisResponse> analyze(@PathVariable Long projectId,
                                                          @Valid @RequestBody GisAnalyzeRequest request) {
        return ResponseEntity.ok(gisService.analyze(projectId, request.address()));
    }

    @GetMapping
    public ResponseEntity<SiteAnalysisResponse> get(@PathVariable Long projectId) {
        return ResponseEntity.ok(gisService.getForProject(projectId));
    }
}
