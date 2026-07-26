package com.rera.auditor.service;

import com.rera.auditor.dto.BuildingResponse;
import com.rera.auditor.dto.FloorResponse;
import com.rera.auditor.entity.Building;
import com.rera.auditor.entity.Floor;
import com.rera.auditor.mapper.BuildingMapper;
import com.rera.auditor.repository.BuildingComponentRepository;
import com.rera.auditor.repository.BuildingRepository;
import com.rera.auditor.repository.FloorRepository;
import com.rera.auditor.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final BuildingComponentRepository componentRepository;
    private final BuildingMapper buildingMapper;

    public BuildingService(BuildingRepository buildingRepository, FloorRepository floorRepository,
                            RoomRepository roomRepository, BuildingComponentRepository componentRepository,
                            BuildingMapper buildingMapper) {
        this.buildingRepository = buildingRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.componentRepository = componentRepository;
        this.buildingMapper = buildingMapper;
    }

    /** Returns the full building -> floors -> rooms/components tree for the 3D viewer and dashboard. */
    public List<BuildingResponse> getBuildingsForProject(Long projectId) {
        List<Building> buildings = buildingRepository.findByProjectId(projectId);
        return buildings.stream().map(this::toFullResponse).toList();
    }

    private BuildingResponse toFullResponse(Building building) {
        List<Floor> floors = floorRepository.findByBuildingIdOrderByFloorNumberAsc(building.getId());
        List<FloorResponse> floorResponses = floors.stream().map(floor -> {
            var rooms = roomRepository.findByFloorId(floor.getId()).stream()
                .map(buildingMapper::toResponse).toList();
            var components = componentRepository.findByFloorId(floor.getId()).stream()
                .map(buildingMapper::toResponse).toList();
            return buildingMapper.toResponse(floor, rooms, components);
        }).toList();
        return buildingMapper.toResponse(building, floorResponses);
    }
}
