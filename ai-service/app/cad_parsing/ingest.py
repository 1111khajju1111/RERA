"""Persists parsed DXF output into the shared Postgres schema."""

from sqlalchemy.orm import Session

from app.models import Building, Floor, Room, BuildingComponent


def ingest_parsed_dxf(db: Session, project_id: int, parsed: dict) -> dict:
    """
    Creates a Building (if none exists yet for this project) with a single
    Floor (floor_number=0 — see dxf_parser.py docstring on the one-file-one-floor
    limitation), then writes the parsed rooms and components under it.

    IMPORTANT: this DELETES the floor's existing rooms/components before
    inserting the newly parsed ones. Without this, re-uploading a revised
    DXF for the same project would just APPEND more rooms/components on
    top of the old ones every time — silently doubling (tripling, ...)
    the floor area and component counts with each re-upload, corrupting
    the compliance score in a way that would be very confusing to debug
    later. Each upload is meant to represent the CURRENT state of the
    building, not an accumulating log of every version's geometry.

    This does mean there's no way to recover a prior version's exact
    geometry once superseded — see ProjectVersionService's docstring
    (Java backend) for why true per-version geometry snapshots are a
    separate, larger schema change, deferred for now.
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
        # Clear prior geometry for this floor before re-ingesting. Any
        # OPEN violation referencing a now-deleted component/floor loses
        # that specific link (ON DELETE SET NULL — see V1__init_schema.sql),
        # not an error; the rule engine re-run right after this regenerates
        # fresh violations against the new geometry anyway.
        db.query(Room).filter(Room.floor_id == floor.id).delete(synchronize_session=False)
        db.query(BuildingComponent).filter(BuildingComponent.floor_id == floor.id).delete(synchronize_session=False)
        db.flush()

    rooms_created = 0
    for room_data in parsed["rooms"]:
        db.add(Room(floor_id=floor.id, **room_data))
        rooms_created += 1

    components_created = 0
    for comp_data in parsed["components"]:
        db.add(BuildingComponent(floor_id=floor.id, detected_by="layer-heuristic-v1", **comp_data))
        components_created += 1

    db.flush()

    # Recompute building-level aggregates now that we have fresh floor data.
    fresh_rooms = db.query(Room).filter(Room.floor_id == floor.id).all()
    total_floor_area = sum((r.area_sqm or 0) for r in fresh_rooms) if fresh_rooms else None
    if total_floor_area:
        building.built_up_area_sqm = total_floor_area

    db.commit()
    return {"floors_created": 1 if floor_created else 0, "rooms_created": rooms_created, "components_created": components_created}
