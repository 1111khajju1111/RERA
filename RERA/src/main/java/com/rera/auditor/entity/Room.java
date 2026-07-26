package com.rera.auditor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(name = "room_type", nullable = false, length = 50)
    private String roomType;

    @Column(name = "area_sqm", precision = 8, scale = 2)
    private BigDecimal areaSqm;

    @Column(name = "width_m", precision = 6, scale = 2)
    private BigDecimal widthM;

    @Column(name = "length_m", precision = 6, scale = 2)
    private BigDecimal lengthM;

    @Column(name = "has_natural_light")
    private Boolean hasNaturalLight = false;

    @Column(name = "has_ventilation")
    private Boolean hasVentilation = false;

    public Room() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Floor getFloor() { return floor; }
    public void setFloor(Floor floor) { this.floor = floor; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public BigDecimal getAreaSqm() { return areaSqm; }
    public void setAreaSqm(BigDecimal areaSqm) { this.areaSqm = areaSqm; }
    public BigDecimal getWidthM() { return widthM; }
    public void setWidthM(BigDecimal widthM) { this.widthM = widthM; }
    public BigDecimal getLengthM() { return lengthM; }
    public void setLengthM(BigDecimal lengthM) { this.lengthM = lengthM; }
    public Boolean getHasNaturalLight() { return hasNaturalLight; }
    public void setHasNaturalLight(Boolean hasNaturalLight) { this.hasNaturalLight = hasNaturalLight; }
    public Boolean getHasVentilation() { return hasVentilation; }
    public void setHasVentilation(Boolean hasVentilation) { this.hasVentilation = hasVentilation; }
}
