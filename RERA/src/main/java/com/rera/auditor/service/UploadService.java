package com.rera.auditor.service;

import com.rera.auditor.entity.Project;
import com.rera.auditor.entity.ProjectVersion;
import com.rera.auditor.repository.ProjectVersionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class UploadService {

    private final ProjectVersionRepository projectVersionRepository;
    private final ProjectService projectService;
    private final AiServiceClient aiServiceClient;
    private final String uploadDir;

    public UploadService(ProjectVersionRepository projectVersionRepository,
                          ProjectService projectService,
                          AiServiceClient aiServiceClient,
                          @Value("${file-storage.upload-dir}") String uploadDir) {
        this.projectVersionRepository = projectVersionRepository;
        this.projectService = projectService;
        this.aiServiceClient = aiServiceClient;
        this.uploadDir = uploadDir;
    }

    @Transactional
    public ProjectVersion storeUpload(Long projectId, MultipartFile file) {
        Project project = projectService.findProjectOrThrow(projectId);

        String fileType = extractFileType(file.getOriginalFilename());
        int nextVersion = projectVersionRepository.findTopByProjectIdOrderByVersionNumberDesc(projectId)
            .map(v -> v.getVersionNumber() + 1).orElse(1);

        String storedFilename = "project_" + projectId + "_v" + nextVersion + "_" + file.getOriginalFilename();
        Path targetPath = Path.of(uploadDir, storedFilename);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file: " + e.getMessage(), e);
        }

        ProjectVersion version = new ProjectVersion();
        version.setProject(project);
        version.setVersionNumber(nextVersion);
        version.setFilePath(targetPath.toString());
        version.setOriginalFilename(file.getOriginalFilename());
        version.setFileType(fileType);
        ProjectVersion saved = projectVersionRepository.save(version);

        project.setStatus("PROCESSING");
        triggerAiPipeline(projectId, targetPath.toString());

        return saved;
    }

    /** Fire-and-forget call to the AI service so the upload endpoint returns immediately. */
    @Async
    public void triggerAiPipeline(Long projectId, String filePath) {
        try {
            aiServiceClient.parseCad(projectId, filePath);
            aiServiceClient.runCompliance(projectId);
            aiServiceClient.generateSuggestions(projectId);
            projectService.updateStatus(projectId, "AUDITED");
        } catch (Exception e) {
            projectService.updateStatus(projectId, "PROCESSING_FAILED");
        }
    }

    private String extractFileType(String filename) {
        if (filename == null || !filename.contains(".")) return "UNKNOWN";
        return filename.substring(filename.lastIndexOf('.') + 1).toUpperCase();
    }
}
