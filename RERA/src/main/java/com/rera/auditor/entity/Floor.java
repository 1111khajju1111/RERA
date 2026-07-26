package com.rera.auditor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "floors")
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name = "floor_height_m", precision = 5, scale = 2)
    private BigDecimal floorHeightM;

    @Column(name = "floor_area_sqm", precision = 10, scale = 2)
    private BigDecimal floorAreaSqm;

    public Floor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }
    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
    public BigDecimal getFloorHeightM() { return floorHeightM; }
    public void setFloorHeightM(BigDecimal floorHeightM) { this.floorHeightM = floorHeightM; }
    public BigDecimal getFloorAreaSqm() { return floorAreaSqm; }
    public void setFloorAreaSqm(BigDecimal floorAreaSqm) { this.floorAreaSqm = floorAreaSqm; }
}
