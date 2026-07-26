package com.rera.auditor.repository;

import com.rera.auditor.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FloorRepository extends JpaRepository<Floor, Long> {
    List<Floor> findByBuildingIdOrderByFloorNumberAsc(Long buildingId);
}
