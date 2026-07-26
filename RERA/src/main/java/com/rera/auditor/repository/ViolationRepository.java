package com.rera.auditor.repository;

import com.rera.auditor.entity.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ViolationRepository extends JpaRepository<Violation, Long> {
    List<Violation> findByProjectIdOrderBySeverityAscDetectedAtDesc(Long projectId);
    List<Violation> findByProjectIdAndStatus(Long projectId, String status);
    long countByProjectIdAndStatus(Long projectId, String status);
}
