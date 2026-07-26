package com.rera.auditor.mapper;

import com.rera.auditor.dto.*;
import com.rera.auditor.entity.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BuildingMapper {

    public RoomResponse toResponse(Room r) {
        return new RoomResponse(r.getId(), r.getRoomType(), r.getAreaSqm(),
            r.getWidthM(), r.getLengthM(), r.getHasNaturalLight(), r.getHasVentilation());
    }

    public ComponentResponse toResponse(BuildingComponent c) {
        return new ComponentResponse(c.getId(), c.getComponentType(), c.getGeometryJson(),
            c.getPosX(), c.getPosY(), c.getWidth(), c.getHeight(),
            c.getMaterial(), c.getConfidenceScore(), c.getDetectedBy());
    }

    public FloorResponse toResponse(Floor f, List<RoomResponse> rooms, List<ComponentResponse> components) {
        return new FloorResponse(f.getId(), f.getFloorNumber(), f.getFloorHeightM(),
            f.getFloorAreaSqm(), rooms, components);
    }

    public BuildingResponse toResponse(Building b, List<FloorResponse> floors) {
        return new BuildingResponse(b.getId(), b.getName(), b.getBuildingType(), b.getNumFloors(),
            b.getHeightM(), b.getBuiltUpAreaSqm(), b.getFarCalculated(), b.getGroundCoveragePct(), floors);
    }
}
