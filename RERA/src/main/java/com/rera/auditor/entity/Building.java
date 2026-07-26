package com.rera.auditor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "buildings")
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "building_type", nullable = false, length = 50)
    private String buildingType;

    @Column(name = "num_floors", nullable = false)
    private Integer numFloors;

    @Column(name = "height_m", precision = 6, scale = 2)
    private BigDecimal heightM;

    @Column(name = "built_up_area_sqm", precision = 10, scale = 2)
    private BigDecimal builtUpAreaSqm;

    @Column(name = "far_calculated", precision = 6, scale = 3)
    private BigDecimal farCalculated;

    @Column(name = "ground_coverage_pct", precision = 5, scale = 2)
    private BigDecimal groundCoveragePct;

    public Building() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBuildingType() { return buildingType; }
    public void setBuildingType(String buildingType) { this.buildingType = buildingType; }
    public Integer getNumFloors() { return numFloors; }
    public void setNumFloors(Integer numFloors) { this.numFloors = numFloors; }
    public BigDecimal getHeightM() { return heightM; }
    public void setHeightM(BigDecimal heightM) { this.heightM = heightM; }
    public BigDecimal getBuiltUpAreaSqm() { return builtUpAreaSqm; }
    public void setBuiltUpAreaSqm(BigDecimal builtUpAreaSqm) { this.builtUpAreaSqm = builtUpAreaSqm; }
    public BigDecimal getFarCalculated() { return farCalculated; }
    public void setFarCalculated(BigDecimal farCalculated) { this.farCalculated = farCalculated; }
    public BigDecimal getGroundCoveragePct() { return groundCoveragePct; }
    public void setGroundCoveragePct(BigDecimal groundCoveragePct) { this.groundCoveragePct = groundCoveragePct; }
}
