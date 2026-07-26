package com.rera.auditor.service;

import com.rera.auditor.dto.SiteAnalysisResponse;
import com.rera.auditor.exception.ResourceNotFoundException;
import com.rera.auditor.mapper.SiteAnalysisMapper;
import com.rera.auditor.repository.SiteAnalysisRepository;
import org.springframework.stereotype.Service;

@Service
public class GisService {

    private final AiServiceClient aiServiceClient;
    private final SiteAnalysisRepository siteAnalysisRepository;
    private final ProjectService projectService;
    private final SiteAnalysisMapper mapper;

    public GisService(AiServiceClient aiServiceClient, SiteAnalysisRepository siteAnalysisRepository,
                       ProjectService projectService, SiteAnalysisMapper mapper) {
        this.aiServiceClient = aiServiceClient;
        this.siteAnalysisRepository = siteAnalysisRepository;
        this.projectService = projectService;
        this.mapper = mapper;
    }

    /** Triggers the AI service's GIS analysis (writes directly to site_analysis), then reads the result back. */
    public SiteAnalysisResponse analyze(Long projectId, String address) {
        projectService.findProjectOrThrow(projectId); // 404s cleanly if the project doesn't exist
        aiServiceClient.analyzeGis(projectId, address);
        return getForProject(projectId);
    }

    public SiteAnalysisResponse getForProject(Long projectId) {
        var siteAnalysis = siteAnalysisRepository.findByProjectId(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("No GIS analysis yet for project " + projectId));
        return mapper.toResponse(siteAnalysis);
    }
}
