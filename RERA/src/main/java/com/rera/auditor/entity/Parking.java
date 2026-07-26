package com.rera.auditor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "parking")
public class Parking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "required_spaces", nullable = false)
    private Integer requiredSpaces;

    @Column(name = "provided_spaces", nullable = false)
    private Integer providedSpaces;

    @Column(name = "parking_ratio", precision = 6, scale = 3)
    private BigDecimal parkingRatio;

    @Column(nullable = false)
    private Boolean compliant;

    public Parking() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }
    public Integer getRequiredSpaces() { return requiredSpaces; }
    public void setRequiredSpaces(Integer requiredSpaces) { this.requiredSpaces = requiredSpaces; }
    public Integer getProvidedSpaces() { return providedSpaces; }
    public void setProvidedSpaces(Integer providedSpaces) { this.providedSpaces = providedSpaces; }
    public BigDecimal getParkingRatio() { return parkingRatio; }
    public void setParkingRatio(BigDecimal parkingRatio) { this.parkingRatio = parkingRatio; }
    public Boolean getCompliant() { return compliant; }
    public void setCompliant(Boolean compliant) { this.compliant = compliant; }
}
