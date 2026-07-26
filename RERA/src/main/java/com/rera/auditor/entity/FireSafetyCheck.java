package com.rera.auditor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "fire_safety_checks")
public class FireSafetyCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "check_type", nullable = false, length = 50)
    private String checkType;

    @Column(name = "required_value", precision = 10, scale = 3)
    private BigDecimal requiredValue;

    @Column(name = "actual_value", precision = 10, scale = 3)
    private BigDecimal actualValue;

    @Column(nullable = false)
    private Boolean compliant;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public FireSafetyCheck() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }
    public String getCheckType() { return checkType; }
    public void setCheckType(String checkType) { this.checkType = checkType; }
    public BigDecimal getRequiredValue() { return requiredValue; }
    public void setRequiredValue(BigDecimal requiredValue) { this.requiredValue = requiredValue; }
    public BigDecimal getActualValue() { return actualValue; }
    public void setActualValue(BigDecimal actualValue) { this.actualValue = actualValue; }
    public Boolean getCompliant() { return compliant; }
    public void setCompliant(Boolean compliant) { this.compliant = compliant; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
