package com.rera.auditor.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;

@Entity
@Table(name = "building_components")
public class BuildingComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(name = "component_type", nullable = false, length = 30)
    private String componentType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geometry_json", nullable = false, columnDefinition = "jsonb")
    private String geometryJson;

    @Column(name = "pos_x", precision = 10, scale = 3)
    private BigDecimal posX;

    @Column(name = "pos_y", precision = 10, scale = 3)
    private BigDecimal posY;

    @Column(precision = 8, scale = 3)
    private BigDecimal width;

    @Column(precision = 8, scale = 3)
    private BigDecimal height;

    @Column(length = 50)
    private String material;

    @Column(name = "confidence_score", precision = 4, scale = 3)
    private BigDecimal confidenceScore;

    @Column(name = "detected_by", length = 50)
    private String detectedBy;

    public BuildingComponent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Floor getFloor() { return floor; }
    public void setFloor(Floor floor) { this.floor = floor; }
    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }
    public String getGeometryJson() { return geometryJson; }
    public void setGeometryJson(String geometryJson) { this.geometryJson = geometryJson; }
    public BigDecimal getPosX() { return posX; }
    public void setPosX(BigDecimal posX) { this.posX = posX; }
    public BigDecimal getPosY() { return posY; }
    public void setPosY(BigDecimal posY) { this.posY = posY; }
    public BigDecimal getWidth() { return width; }
    public void setWidth(BigDecimal width) { this.width = width; }
    public BigDecimal getHeight() { return height; }
    public void setHeight(BigDecimal height) { this.height = height; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getDetectedBy() { return detectedBy; }
    public void setDetectedBy(String detectedBy) { this.detectedBy = detectedBy; }
}
