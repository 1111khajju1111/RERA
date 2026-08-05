from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
import json

from app.db import get_db
from app.schemas import GisAnalyzeRequest, GisAnalyzeResponse
from app.models import SiteAnalysis, AuditReport
from app.gis.geocode import geocode_address, GeocodeError
from app.gis.roads import fetch_nearby_roads, RoadDataError
from app.gis.encroachment import check_encroachment
from app.gis.fire_access import evaluate_and_persist_fire_access
from app.rules_engine.scoring import compute_score

router = APIRouter(tags=["gis"])


@router.post("/gis/analyze", response_model=GisAnalyzeResponse)
def analyze(request: GisAnalyzeRequest, db: Session = Depends(get_db)):
    warnings = []

    try:
        geocoded = geocode_address(request.address)
    except GeocodeError as e:
        raise HTTPException(status_code=502, detail=str(e))
    if geocoded is None:
        raise HTTPException(status_code=404, detail=f"Could not geocode address: {request.address}")

    road_data = None
    try:
        road_data = fetch_nearby_roads(geocoded["latitude"], geocoded["longitude"])
    except RoadDataError as e:
        raise HTTPException(status_code=502, detail=str(e))
    nearest = road_data["nearest"]
    if nearest is None:
        warnings.append("No roads found within 150m — try a more precise address, or the area may be unmapped in OSM.")

    encroachment = check_encroachment(request.project_id)

    site = db.query(SiteAnalysis).filter(SiteAnalysis.project_id == request.project_id).first()
    if site is None:
        site = SiteAnalysis(project_id=request.project_id)
        db.add(site)

    site.latitude = geocoded["latitude"]
    site.longitude = geocoded["longitude"]
    site.geocoded_address = geocoded["display_name"]
    site.nearby_roads_geojson = road_data["geojson"]
    site.encroachment_status = encroachment["status"]
    site.encroachment_notes = encroachment["notes"]

    if nearest:
        site.nearest_road_distance_m = nearest["distance_m"]
        site.nearest_road_width_m = nearest["width_m"]
        site.nearest_road_width_is_estimated = nearest["width_is_estimated"]
        site.nearest_road_type = nearest["highway_type"]
        site.nearest_road_name = nearest["name"]
    else:
        site.nearest_road_distance_m = None
        site.nearest_road_width_m = None

    db.flush()  # ensure site.id exists before the fire-access evaluator reads it

    fire_compliant = evaluate_and_persist_fire_access(db, request.project_id, site) if nearest else None
    site.fire_access_compliant = fire_compliant

    # A GIS analysis can add/remove CRITICAL fire-access violations
    # independently of the DXF pipeline (e.g. re-running this after moving
    # to a different address), so re-snapshot the score here too — not
    # just after /run-compliance — otherwise the dashboard score would go
    # stale until the next full DXF re-analysis.
    score, approval_probability = compute_score(db, request.project_id)
    db.add(AuditReport(
        project_id=request.project_id,
        compliance_score=score,
        approval_probability=approval_probability,
        format="SNAPSHOT",
    ))

    db.commit()
    db.refresh(site)

    return GisAnalyzeResponse(
        project_id=request.project_id,
        latitude=float(site.latitude) if site.latitude else None,
        longitude=float(site.longitude) if site.longitude else None,
        geocoded_address=site.geocoded_address,
        nearest_road_distance_m=float(site.nearest_road_distance_m) if site.nearest_road_distance_m else None,
        nearest_road_width_m=float(site.nearest_road_width_m) if site.nearest_road_width_m else None,
        fire_access_compliant=site.fire_access_compliant,
        encroachment_status=site.encroachment_status,
        warnings=warnings,
    )
