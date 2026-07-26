package com.rera.auditor.service;

import com.rera.auditor.dto.ComplianceHistoryEntry;
import com.rera.auditor.dto.ComplianceRuleResponse;
import com.rera.auditor.dto.ComplianceSummaryResponse;
import com.rera.auditor.entity.AuditReport;
import com.rera.auditor.mapper.ComplianceRuleMapper;
import com.rera.auditor.repository.AuditReportRepository;
import com.rera.auditor.repository.ComplianceRuleRepository;
import com.rera.auditor.repository.ViolationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Phase 8 refinement: scoring is now computed ONCE, by the AI service,
 * right after it runs the rule engine (see ai-service/app/rules_engine/
 * scoring.py) — it has the freshest violation set in hand at that exact
 * moment and writes a snapshot to audit_reports. This service used to
 * recompute the same score independently in Java from live violation
 * counts, which meant two implementations of the same formula that could
 * silently drift apart. Now this just reads the latest snapshot and adds
 * the live violation breakdown (counts by severity) for display — the
 * breakdown is cheap to compute fresh and doesn't need to be "frozen" at
 * analysis time the way the headline score does.
 */
@Service
public class ComplianceService {

    private final ComplianceRuleRepository ruleRepository;
    private final ViolationRepository violationRepository;
    private final AuditReportRepository auditReportRepository;
    private final ComplianceRuleMapper ruleMapper;

    public ComplianceService(ComplianceRuleRepository ruleRepository, ViolationRepository violationRepository,
                              AuditReportRepository auditReportRepository, ComplianceRuleMapper ruleMapper) {
        this.ruleRepository = ruleRepository;
        this.violationRepository = violationRepository;
        this.auditReportRepository = auditReportRepository;
        this.ruleMapper = ruleMapper;
    }

    public List<ComplianceRuleResponse> listRules() {
        return ruleRepository.findAll().stream().map(ruleMapper::toResponse).toList();
    }

    public ComplianceSummaryResponse getSummary(Long projectId) {
        long critical = violationRepository.findByProjectIdAndStatus(projectId, "OPEN").stream()
            .filter(v -> "CRITICAL".equals(v.getSeverity())).count();
        long major = violationRepository.findByProjectIdAndStatus(projectId, "OPEN").stream()
            .filter(v -> "MAJOR".equals(v.getSeverity())).count();
        long minor = violationRepository.findByProjectIdAndStatus(projectId, "OPEN").stream()
            .filter(v -> "MINOR".equals(v.getSeverity())).count();
        long total = critical + major + minor;

        List<AuditReport> history = auditReportRepository.findByProjectIdOrderByGeneratedAtDesc(projectId);
        if (history.isEmpty()) {
            // No compliance run has happened yet for this project — 100/100
            // "no violations found" would be misleading (nothing was
            // actually checked). Surface a clearly-null state instead.
            return new ComplianceSummaryResponse(null, null, total, critical, major, minor);
        }

        AuditReport latest = history.get(0);
        return new ComplianceSummaryResponse(
            latest.getComplianceScore(), latest.getApprovalProbability(), total, critical, major, minor
        );
    }

    public List<ComplianceHistoryEntry> getHistory(Long projectId) {
        return auditReportRepository.findByProjectIdOrderByGeneratedAtDesc(projectId).stream()
            .map(r -> new ComplianceHistoryEntry(r.getComplianceScore(), r.getApprovalProbability(), r.getGeneratedAt()))
            .toList();
    }
}
