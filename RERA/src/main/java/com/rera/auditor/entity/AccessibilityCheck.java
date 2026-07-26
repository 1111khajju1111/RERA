package com.rera.auditor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "accessibility_checks")
public class AccessibilityCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "check_type", nullable = false, length = 50)
    private String checkType;

    @Column(nullable = false)
    private Boolean compliant;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public AccessibilityCheck() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }
    public String getCheckType() { return checkType; }
    public void setCheckType(String checkType) { this.checkType = checkType; }
    public Boolean getCompliant() { return compliant; }
    public void setCompliant(Boolean compliant) { this.compliant = compliant; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
