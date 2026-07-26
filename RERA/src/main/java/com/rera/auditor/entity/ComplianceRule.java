package com.rera.auditor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "compliance_rules")
public class ComplianceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 30)
    private String ruleCode;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String parameter;

    @Column(nullable = false, length = 10)
    private String operator;

    @Column(name = "threshold_value", nullable = false, precision = 10, scale = 3)
    private BigDecimal thresholdValue;

    @Column(length = 20)
    private String unit;

    @Column(name = "applicable_building_type", length = 50)
    private String applicableBuildingType;

    @Column(name = "source_reference", length = 255)
    private String sourceReference;

    @Column(name = "default_severity", nullable = false, length = 20)
    private String defaultSeverity;

    public ComplianceRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getParameter() { return parameter; }
    public void setParameter(String parameter) { this.parameter = parameter; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getApplicableBuildingType() { return applicableBuildingType; }
    public void setApplicableBuildingType(String applicableBuildingType) { this.applicableBuildingType = applicableBuildingType; }
    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }
    public String getDefaultSeverity() { return defaultSeverity; }
    public void setDefaultSeverity(String defaultSeverity) { this.defaultSeverity = defaultSeverity; }
}
