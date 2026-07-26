package com.rera.auditor.repository;

import com.rera.auditor.entity.BuildingComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BuildingComponentRepository extends JpaRepository<BuildingComponent, Long> {
    List<BuildingComponent> findByFloorId(Long floorId);
    List<BuildingComponent> findByFloorIdIn(List<Long> floorIds);
    List<BuildingComponent> findByFloorIdAndComponentType(Long floorId, String componentType);
}
