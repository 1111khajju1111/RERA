package com.rera.auditor.repository;

import com.rera.auditor.entity.AuditReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AuditReportRepository extends JpaRepository<AuditReport, Long> {
    List<AuditReport> findByProjectIdOrderByGeneratedAtDesc(Long projectId);

    // Most recent snapshot for a given version, in case a version was
    // re-analyzed more than once.
    Optional<AuditReport> findTopByProjectVersionIdOrderByGeneratedAtDesc(Long projectVersionId);
}
