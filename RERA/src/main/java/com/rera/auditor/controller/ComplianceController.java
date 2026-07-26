package com.rera.auditor.controller;

import com.rera.auditor.dto.ComplianceHistoryEntry;
import com.rera.auditor.dto.ComplianceRuleResponse;
import com.rera.auditor.dto.ComplianceSummaryResponse;
import com.rera.auditor.service.ComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @GetMapping("/api/rules")
    public ResponseEntity<List<ComplianceRuleResponse>> listRules() {
        return ResponseEntity.ok(complianceService.listRules());
    }

    @GetMapping("/api/projects/{projectId}/compliance-score")
    public ResponseEntity<ComplianceSummaryResponse> getSummary(@PathVariable Long projectId) {
        return ResponseEntity.ok(complianceService.getSummary(projectId));
    }

    @GetMapping("/api/projects/{projectId}/compliance-history")
    public ResponseEntity<List<ComplianceHistoryEntry>> getHistory(@PathVariable Long projectId) {
        return ResponseEntity.ok(complianceService.getHistory(projectId));
    }
}
