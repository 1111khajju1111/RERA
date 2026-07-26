package com.rera.auditor.mapper;

import com.rera.auditor.dto.ComplianceRuleResponse;
import com.rera.auditor.entity.ComplianceRule;
import org.springframework.stereotype.Component;

@Component
public class ComplianceRuleMapper {
    public ComplianceRuleResponse toResponse(ComplianceRule r) {
        return new ComplianceRuleResponse(r.getId(), r.getRuleCode(), r.getCategory(), r.getDescription(),
            r.getParameter(), r.getOperator(), r.getThresholdValue(), r.getUnit(),
            r.getApplicableBuildingType(), r.getSourceReference(), r.getDefaultSeverity());
    }
}
