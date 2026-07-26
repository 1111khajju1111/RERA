package com.rera.auditor.repository;

import com.rera.auditor.entity.AccessibilityCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccessibilityCheckRepository extends JpaRepository<AccessibilityCheck, Long> {
    List<AccessibilityCheck> findByBuildingId(Long buildingId);
}
