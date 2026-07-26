package com.rera.auditor.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "site_analysis")
public class SiteAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "geocoded_address", columnDefinition = "TEXT")
    private String geocodedAddress;

    @Column(name = "nearest_road_distance_m", precision = 8, scale = 2)
    private BigDecimal nearestRoadDistanceM;

    @Column(name = "nearest_road_width_m", precision = 6, scale = 2)
    private BigDecimal nearestRoadWidthM;

    @Column(name = "nearest_road_width_is_estimated")
    private Boolean nearestRoadWidthIsEstimated = true;

    @Column(name = "nearest_road_type", length = 50)
    private String nearestRoadType;

    @Column(name = "nearest_road_name", length = 255)
    private String nearestRoadName;

    @Column(name = "fire_access_compliant")
    private Boolean fireAccessCompliant;

    @Column(name = "encroachment_status", nullable = false, length = 30)
    private String encroachmentStatus = "NOT_AVAILABLE";

    @Column(name = "encroachment_notes", columnDefinition = "TEXT")
    private String encroachmentNotes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nearby_roads_geojson", columnDefinition = "jsonb")
    private String nearbyRoadsGeojson;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt = LocalDateTime.now();

    public SiteAnalysis() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getGeocodedAddress() { return geocodedAddress; }
    public void setGeocodedAddress(String geocodedAddress) { this.geocodedAddress = geocodedAddress; }
    public BigDecimal getNearestRoadDistanceM() { return nearestRoadDistanceM; }
    public void setNearestRoadDistanceM(BigDecimal v) { this.nearestRoadDistanceM = v; }
    public BigDecimal getNearestRoadWidthM() { return nearestRoadWidthM; }
    public void setNearestRoadWidthM(BigDecimal v) { this.nearestRoadWidthM = v; }
    public Boolean getNearestRoadWidthIsEstimated() { return nearestRoadWidthIsEstimated; }
    public void setNearestRoadWidthIsEstimated(Boolean v) { this.nearestRoadWidthIsEstimated = v; }
    public String getNearestRoadType() { return nearestRoadType; }
    public void setNearestRoadType(String v) { this.nearestRoadType = v; }
    public String getNearestRoadName() { return nearestRoadName; }
    public void setNearestRoadName(String v) { this.nearestRoadName = v; }
    public Boolean getFireAccessCompliant() { return fireAccessCompliant; }
    public void setFireAccessCompliant(Boolean v) { this.fireAccessCompliant = v; }
    public String getEncroachmentStatus() { return encroachmentStatus; }
    public void setEncroachmentStatus(String v) { this.encroachmentStatus = v; }
    public String getEncroachmentNotes() { return encroachmentNotes; }
    public void setEncroachmentNotes(String v) { this.encroachmentNotes = v; }
    public String getNearbyRoadsGeojson() { return nearbyRoadsGeojson; }
    public void setNearbyRoadsGeojson(String v) { this.nearbyRoadsGeojson = v; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime v) { this.analyzedAt = v; }
}
