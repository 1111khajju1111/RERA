package com.rera.auditor.mapper;

import com.rera.auditor.dto.ViolationResponse;
import com.rera.auditor.entity.Violation;
import org.springframework.stereotype.Component;

@Component
public class ViolationMapper {
    public ViolationResponse toResponse(Violation v) {
        return new ViolationResponse(
            v.getId(),
            v.getRule().getRuleCode(),
            v.getRule().getCategory(),
            v.getSeverity(),
            v.getDescription(),
            v.getDetectedValue(),
            v.getRequiredValue(),
            v.getRule().getUnit(),
            v.getStatus(),
            v.getFloor() != null ? v.getFloor().getFloorNumber() : null,
            v.getComponent() != null ? v.getComponent().getComponentType() : null,
            v.getComponent() != null ? v.getComponent().getId() : null,
            v.getResolutionNote(),
            v.getDetectedAt()
        );
    }
}
