package com.rera.auditor.mapper;

import com.rera.auditor.dto.ProjectResponse;
import com.rera.auditor.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    public ProjectResponse toResponse(Project p) {
        return new ProjectResponse(
            p.getId(), p.getName(), p.getDescription(), p.getLocation(),
            p.getPlotAreaSqm(), p.getStatus(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
