package com.rera.auditor.repository;

import com.rera.auditor.entity.FireSafetyCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FireSafetyCheckRepository extends JpaRepository<FireSafetyCheck, Long> {
    List<FireSafetyCheck> findByBuildingId(Long buildingId);
}
