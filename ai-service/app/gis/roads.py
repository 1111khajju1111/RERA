"""
Real nearby-road data from OpenStreetMap's Overpass API. Same public-
instance caveats as geocode.py apply: fair-use only, no bulk queries,
self-host Overpass for production
(https://wiki.openstreetmap.org/wiki/Overpass_API#Public_Overpass_API_instances).

Width handling is honest about what's measured vs. estimated: OSM's
`width` tag exists on some ways but is present on a small minority of
roads in most regions. When absent, we fall back to a documented
per-highway-type assumption table — this is flagged in the response via
`width_is_estimated`, and the frontend surfaces that flag rather than
presenting an estimate as a measurement.
"""

import math
import httpx

OVERPASS_URL = "https://overpass-api.de/api/interpreter"

# NBC/IRC road classifications differ from OSM's, so this is an approximate
# mapping, not an authoritative one. Source it against IRC width standards
# for anything beyond a demo.
ESTIMATED_WIDTH_BY_HIGHWAY_TYPE = {
    "motorway": 15.0, "trunk": 12.0, "primary": 10.0, "secondary": 8.0,
    "tertiary": 6.0, "residential": 5.0, "service": 3.5, "unclassified": 4.5,
    "living_street": 4.0, "footway": 1.5, "path": 1.0,
}
DEFAULT_ESTIMATED_WIDTH = 4.5


def _local_xy(lat: float, lon: float, ref_lat: float, ref_lon: float) -> tuple[float, float]:
    """Equirectangular projection centered at (ref_lat, ref_lon) — accurate
    enough for distances of a few hundred meters, which is the scale fire
    access checks operate at. Not suitable for long-range distances."""
    R = 6371000.0  # Earth radius, meters
    x = math.radians(lon - ref_lon) * math.cos(math.radians(ref_lat)) * R
    y = math.radians(lat - ref_lat) * R
    return x, y


def _point_to_segment_distance(px, py, ax, ay, bx, by) -> float:
    """Standard 2D point-to-line-segment distance."""
    dx, dy = bx - ax, by - ay
    if dx == 0 and dy == 0:
        return math.hypot(px - ax, py - ay)
    t = max(0, min(1, ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)))
    closest_x, closest_y = ax + t * dx, ay + t * dy
    return math.hypot(px - closest_x, py - closest_y)


def fetch_nearby_roads(latitude: float, longitude: float, radius_m: int = 150) -> dict:
    """
    Returns:
      {
        "nearest": {distance_m, width_m, width_is_estimated, highway_type, name} | None,
        "geojson": {...}  # FeatureCollection of fetched ways, for the Leaflet map
      }
    """
    query = f"""
    [out:json][timeout:15];
    way(around:{radius_m},{latitude},{longitude})["highway"];
    out geom;
    """
    response = httpx.post(OVERPASS_URL, data={"data": query}, timeout=20.0)
    response.raise_for_status()
    data = response.json()

    features = []
    nearest = None
    nearest_distance = float("inf")

    for element in data.get("elements", []):
        if element.get("type") != "way" or "geometry" not in element:
            continue

        tags = element.get("tags", {})
        highway_type = tags.get("highway", "unclassified")
        coords = [(pt["lon"], pt["lat"]) for pt in element["geometry"]]

        features.append({
            "type": "Feature",
            "properties": {"highway": highway_type, "name": tags.get("name"), "width": tags.get("width")},
            "geometry": {"type": "LineString", "coordinates": coords},
        })

        # Distance from query point to this way's nearest segment
        way_min_distance = float("inf")
        for i in range(len(element["geometry"]) - 1):
            a, b = element["geometry"][i], element["geometry"][i + 1]
            ax, ay = _local_xy(a["lat"], a["lon"], latitude, longitude)
            bx, by = _local_xy(b["lat"], b["lon"], latitude, longitude)
            d = _point_to_segment_distance(0, 0, ax, ay, bx, by)
            way_min_distance = min(way_min_distance, d)

        if way_min_distance < nearest_distance:
            nearest_distance = way_min_distance
            width_tag = tags.get("width")
            width_is_estimated = width_tag is None
            width_m = float(width_tag) if width_tag else ESTIMATED_WIDTH_BY_HIGHWAY_TYPE.get(
                highway_type, DEFAULT_ESTIMATED_WIDTH
            )
            nearest = {
                "distance_m": round(way_min_distance, 2),
                "width_m": round(width_m, 2),
                "width_is_estimated": width_is_estimated,
                "highway_type": highway_type,
                "name": tags.get("name"),
            }

    return {
        "nearest": nearest,
        "geojson": {"type": "FeatureCollection", "features": features},
    }
