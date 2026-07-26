package com.rera.auditor.repository;

import com.rera.auditor.entity.SiteAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SiteAnalysisRepository extends JpaRepository<SiteAnalysis, Long> {
    Optional<SiteAnalysis> findByProjectId(Long projectId);
}
