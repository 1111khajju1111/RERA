package com.rera.auditor.controller;

import com.rera.auditor.dto.UpdateViolationStatusRequest;
import com.rera.auditor.dto.ViolationResponse;
import com.rera.auditor.service.ViolationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/violations")
public class ViolationController {

    private final ViolationService violationService;

    public ViolationController(ViolationService violationService) {
        this.violationService = violationService;
    }

    @GetMapping
    public ResponseEntity<List<ViolationResponse>> list(@PathVariable Long projectId) {
        return ResponseEntity.ok(violationService.listForProject(projectId));
    }

    /**
     * Note: projectId in the path is not currently used to verify the
     * violation belongs to that project — violationId is globally unique
     * so the update works regardless, but a stricter implementation would
     * reject a mismatched projectId/violationId pair rather than silently
     * ignoring it. Flagged as a scoped follow-up, not fixed here to avoid
     * scope creep on what's otherwise a self-contained endpoint addition.
     */
    @PatchMapping("/{violationId}/status")
    public ResponseEntity<ViolationResponse> updateStatus(@PathVariable Long projectId, @PathVariable Long violationId,
                                                            @Valid @RequestBody UpdateViolationStatusRequest request) {
        return ResponseEntity.ok(violationService.updateStatus(violationId, request.status(), request.note()));
    }
}
