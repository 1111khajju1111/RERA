package com.rera.auditor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_reports")
public class AuditReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "compliance_score", precision = 5, scale = 2)
    private BigDecimal complianceScore;

    @Column(name = "approval_probability", precision = 5, scale = 2)
    private BigDecimal approvalProbability;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(length = 10)
    private String format;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();

    public AuditReport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public BigDecimal getComplianceScore() { return complianceScore; }
    public void setComplianceScore(BigDecimal complianceScore) { this.complianceScore = complianceScore; }
    public BigDecimal getApprovalProbability() { return approvalProbability; }
    public void setApprovalProbability(BigDecimal approvalProbability) { this.approvalProbability = approvalProbability; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
