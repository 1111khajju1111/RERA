package com.rera.auditor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "violations")
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private ComplianceRule rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    private BuildingComponent component;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "detected_value", precision = 10, scale = 3)
    private BigDecimal detectedValue;

    @Column(name = "required_value", precision = 10, scale = 3)
    private BigDecimal requiredValue;

    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @Column(name = "detected_at", nullable = false, updatable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    public Violation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public ComplianceRule getRule() { return rule; }
    public void setRule(ComplianceRule rule) { this.rule = rule; }
    public BuildingComponent getComponent() { return component; }
    public void setComponent(BuildingComponent component) { this.component = component; }
    public Floor getFloor() { return floor; }
    public void setFloor(Floor floor) { this.floor = floor; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getDetectedValue() { return detectedValue; }
    public void setDetectedValue(BigDecimal detectedValue) { this.detectedValue = detectedValue; }
    public BigDecimal getRequiredValue() { return requiredValue; }
    public void setRequiredValue(BigDecimal requiredValue) { this.requiredValue = requiredValue; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
}
