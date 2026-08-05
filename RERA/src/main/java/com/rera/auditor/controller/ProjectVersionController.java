package com.rera.auditor.controller;

import com.rera.auditor.dto.ProjectVersionResponse;
import com.rera.auditor.service.ProjectVersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/versions")
public class ProjectVersionController {

    private final ProjectVersionService projectVersionService;

    public ProjectVersionController(ProjectVersionService projectVersionService) {
        this.projectVersionService = projectVersionService;
    }

    /** The Project Timeline: every upload, in order, with the compliance score it produced. */
    @GetMapping
    public ResponseEntity<List<ProjectVersionResponse>> list(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectVersionService.listForProject(projectId));
    }

    /** Re-runs the AI pipeline against an already-uploaded file. */
    @PostMapping("/{versionId}/reanalyze")
    public ResponseEntity<Void> reanalyze(@PathVariable Long projectId, @PathVariable Long versionId) {
        projectVersionService.reanalyze(projectId, versionId);
        return ResponseEntity.noContent().build();
    }
}
