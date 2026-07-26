package com.rera.auditor.controller;

import com.rera.auditor.entity.ProjectVersion;
import com.rera.auditor.service.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/projects/{projectId}/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<UploadResponse> upload(@PathVariable Long projectId,
                                                  @RequestParam("file") MultipartFile file) {
        ProjectVersion version = uploadService.storeUpload(projectId, file);
        UploadResponse body = new UploadResponse(
            version.getId(), version.getVersionNumber(), version.getOriginalFilename(),
            version.getFileType(), "PROCESSING"
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    public record UploadResponse(Long versionId, Integer versionNumber, String filename,
                                  String fileType, String processingStatus) {}
}
