package com.rera.auditor.service;

import com.rera.auditor.config.CurrentUserService;
import com.rera.auditor.dto.ProjectRequest;
import com.rera.auditor.dto.ProjectResponse;
import com.rera.auditor.entity.Project;
import com.rera.auditor.entity.User;
import com.rera.auditor.exception.ResourceNotFoundException;
import com.rera.auditor.mapper.ProjectMapper;
import com.rera.auditor.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final CurrentUserService currentUserService;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper,
                           CurrentUserService currentUserService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User user = currentUserService.getCurrentUser();
        Project project = new Project();
        project.setUser(user);
        project.setName(request.name());
        project.setDescription(request.description());
        project.setLocation(request.location());
        project.setPlotAreaSqm(request.plotAreaSqm());
        project.setStatus("DRAFT");
        return projectMapper.toResponse(projectRepository.save(project));
    }

    public List<ProjectResponse> listMyProjects() {
        User user = currentUserService.getCurrentUser();
        return projectRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream().map(projectMapper::toResponse).toList();
    }

    public ProjectResponse getProject(Long projectId) {
        return projectMapper.toResponse(findProjectOrThrow(projectId));
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        Project project = findProjectOrThrow(projectId);
        project.setName(request.name());
        project.setDescription(request.description());
        project.setLocation(request.location());
        project.setPlotAreaSqm(request.plotAreaSqm());
        project.setUpdatedAt(LocalDateTime.now());
        return projectMapper.toResponse(project);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = findProjectOrThrow(projectId);
        projectRepository.delete(project);
    }

    @Transactional
    public void updateStatus(Long projectId, String status) {
        Project project = findProjectOrThrow(projectId);
        project.setStatus(status);
        project.setUpdatedAt(LocalDateTime.now());
    }

    protected Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }
}
