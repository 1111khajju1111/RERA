package com.rera.auditor.service;

import com.rera.auditor.dto.ViolationResponse;
import com.rera.auditor.entity.Violation;
import com.rera.auditor.exception.ResourceNotFoundException;
import com.rera.auditor.mapper.ViolationMapper;
import com.rera.auditor.repository.ViolationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ViolationService {

    private final ViolationRepository violationRepository;
    private final ViolationMapper violationMapper;

    public ViolationService(ViolationRepository violationRepository, ViolationMapper violationMapper) {
        this.violationRepository = violationRepository;
        this.violationMapper = violationMapper;
    }

    public List<ViolationResponse> listForProject(Long projectId) {
        return violationRepository.findByProjectIdOrderBySeverityAscDetectedAtDesc(projectId)
            .stream().map(violationMapper::toResponse).toList();
    }

    /**
     * Transitions a violation to RESOLVED (fixed and re-verified) or WAIVED
     * (accepted as-is, e.g. a local authority granted a variance) or back
     * to OPEN. Only OPEN violations count against the compliance score
     * (see ComplianceService) — WAIVED and RESOLVED are excluded, since
     * the AI service's re-run of the rule engine is idempotent and would
     * otherwise silently resurrect a violation the architect had already
     * addressed through means outside the automated check (e.g. a manual
     * site visit confirming compliance the geometry parser couldn't see).
     */
    @Transactional
    public ViolationResponse updateStatus(Long violationId, String status, String note) {
        Violation violation = violationRepository.findById(violationId)
            .orElseThrow(() -> new ResourceNotFoundException("Violation not found: " + violationId));
        violation.setStatus(status);
        violation.setResolutionNote(note);
        return violationMapper.toResponse(violation);
    }
}
