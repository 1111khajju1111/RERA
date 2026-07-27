"""Persists parsed DXF output into the shared Postgres schema."""

from sqlalchemy.orm import Session

from app.models import Building, Floor, Room, BuildingComponent


def ingest_parsed_dxf(db: Session, project_id: int, parsed: dict) -> dict:
    """
    Creates a Building (if none exists yet for this project) with a single
    Floor (floor_number=0 — see dxf_parser.py docstring on the one-file-one-floor
    limitation), then writes the parsed rooms and components under it.

    Re-uploading a new/revised DXF to the same project replaces that
    floor's rooms and components rather than adding to them — otherwise
    every re-upload would pile new rows on top of the previous version's,
    double-counting area/violations against stale data that no longer
    matches the current drawing.
    """
    building = db.query(Building).filter(Building.project_id == project_id).first()
    if building is None:
        building = Building(
            project_id=project_id,
            name="Building A",
            building_type="RESIDENTIAL",
            num_floors=1,
        )
        db.add(building)
        db.flush()  # get building.id without committing yet

    floor = db.query(Floor).filter(
        Floor.building_id == building.id, Floor.floor_number == 0
    ).first()
    floor_created = floor is None
    if floor is None:
        floor = Floor(building_id=building.id, floor_number=0)
        db.add(floor)
        db.flush()
    else:
        # Re-analysis of this floor: clear out the previous upload's rooms
        # and components before writing this one's, so they don't stack.
        db.query(Room).filter(Room.floor_id == floor.id).delete()
        db.query(BuildingComponent).filter(BuildingComponent.floor_id == floor.id).delete()
        db.flush()

    rooms_created = 0
    for room_data in parsed["rooms"]:
        db.add(Room(floor_id=floor.id, **room_data))
        rooms_created += 1

    components_created = 0
    for comp_data in parsed["components"]:
        db.add(BuildingComponent(floor_id=floor.id, detected_by="layer-heuristic-v1", **comp_data))
        components_created += 1

    # Recompute building-level aggregates now that we have floor data.
    total_floor_area = sum((r.area_sqm or 0) for r in floor.rooms) if floor.rooms else None
    if total_floor_area:
        building.built_up_area_sqm = total_floor_area

    db.commit()
    return {"floors_created": 1 if floor_created else 0, "rooms_created": rooms_created, "components_created": components_created}
