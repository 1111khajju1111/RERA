package com.rera.auditor.repository;

import com.rera.auditor.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParkingRepository extends JpaRepository<Parking, Long> {
    List<Parking> findByBuildingId(Long buildingId);
}
