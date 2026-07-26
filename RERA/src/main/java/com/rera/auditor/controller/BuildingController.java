package com.rera.auditor.controller;

import com.rera.auditor.dto.BuildingResponse;
import com.rera.auditor.service.BuildingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    /** Full building -> floor -> room/component tree, used by the dashboard and 3D viewer. */
    @GetMapping
    public ResponseEntity<List<BuildingResponse>> getBuildings(@PathVariable Long projectId) {
        return ResponseEntity.ok(buildingService.getBuildingsForProject(projectId));
    }
}
