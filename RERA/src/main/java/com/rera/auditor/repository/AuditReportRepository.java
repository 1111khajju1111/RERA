package com.rera.auditor.repository;

import com.rera.auditor.entity.AuditReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditReportRepository extends JpaRepository<AuditReport, Long> {
    List<AuditReport> findByProjectIdOrderByGeneratedAtDesc(Long projectId);
}
