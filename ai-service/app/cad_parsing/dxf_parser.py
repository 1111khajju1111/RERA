"""
Real, working DXF geometry extraction using ezdxf. No ML involved here —
this is deterministic CAD geometry parsing, which is a solid, honest
foundation to build vision-based detection on top of later.

What it does:
  - Opens a DXF, iterates modelspace entities (LINE, LWPOLYLINE)
  - Classifies each entity's layer via layer_mapping.classify_layer
  - For component layers: computes a bounding box -> pos_x, pos_y, width, height
  - For room layers (closed polylines): computes true polygon area via the
    shoelace formula, plus a bounding-box width/length
  - Detects window/room overlap as a ventilation proxy

What it does NOT do (by design, not oversight):
  - No support for DWG (proprietary binary; needs a paid SDK or ODA
    converter to DXF first — LibreDWG's DXF export quality varies)
  - No IFC parsing (see ifc_parser.py stub)
  - No multi-floor detection within one file — one DXF = one floor for now.
    Real multi-storey handling needs either floor-tagged layers or
    separate files per floor; document whichever convention you adopt
    and extend classify_layer() accordingly.
"""

import ezdxf
from ezdxf.document import Drawing

from app.cad_parsing.layer_mapping import classify_layer


def _bounding_box(points: list[tuple[float, float]]) -> dict:
    xs = [p[0] for p in points]
    ys = [p[1] for p in points]
    min_x, max_x = min(xs), max(xs)
    min_y, max_y = min(ys), max(ys)
    return {
        # float(...) here is deliberate, not cosmetic: LWPOLYLINE points
        # come back from ezdxf as numpy.float64 (see _entity_points), and
        # round() on a numpy scalar returns another numpy scalar. Without
        # this cast, psycopg2 can't adapt the value and silently inlines
        # its repr() ("np.float64(0.23)") as bare SQL text, which Postgres
        # then parses as a schema-qualified identifier and fails with
        # 'schema "np" does not exist'.
        "pos_x": float(round(min_x, 3)),
        "pos_y": float(round(min_y, 3)),
        "width": float(round(max_x - min_x, 3)),
        "height": float(round(max_y - min_y, 3)),
    }


def _polygon_area(points: list[tuple[float, float]]) -> float:
    """Shoelace formula. Points should already form a closed loop."""
    n = len(points)
    if n < 3:
        return 0.0
    area = 0.0
    for i in range(n):
        x1, y1 = points[i]
        x2, y2 = points[(i + 1) % n]
        area += x1 * y2 - x2 * y1
    return float(abs(area) / 2.0)


def _entity_points(entity) -> list[tuple[float, float]] | None:
    dxftype = entity.dxftype()
    if dxftype == "LWPOLYLINE":
        # get_points() yields numpy.float64, not plain float — cast here
        # so every downstream consumer (bbox, area, geometry_json) only
        # ever sees native Python floats.
        return [(float(p[0]), float(p[1])) for p in entity.get_points()]
    if dxftype == "LINE":
        start = entity.dxf.start
        end = entity.dxf.end
        return [(start.x, start.y), (end.x, end.y)]
    return None


def parse_dxf(file_path: str) -> dict:
    """
    Returns a structured dict:
    {
      "components": [ {component_type, geometry_json, pos_x, pos_y, width, height} ],
      "rooms": [ {room_type, area_sqm, width_m, length_m, has_natural_light, has_ventilation} ],
      "warnings": [ str ]
    }
    """
    warnings: list[str] = []
    doc: Drawing = ezdxf.readfile(file_path)
    msp = doc.modelspace()

    components = []
    room_candidates = []  # (room_type, points)

    for entity in msp:
        layer = entity.dxf.layer
        kind, sub_type = classify_layer(layer)
        points = _entity_points(entity)
        if points is None:
            continue

        if kind == "component":
            bbox = _bounding_box(points)
            components.append({
                "component_type": sub_type,
                "geometry_json": {"shape": entity.dxftype().lower(), "points": points},
                **bbox,
            })
        elif kind == "room":
            room_candidates.append((sub_type, points))
        else:
            warnings.append(f"Unclassified layer '{layer}' on a {entity.dxftype()} entity — skipped")

    window_boxes = [
        (c["pos_x"], c["pos_y"], c["pos_x"] + c["width"], c["pos_y"] + c["height"])
        for c in components if c["component_type"] == "WINDOW"
    ]

    rooms = []
    for room_type, points in room_candidates:
        bbox = _bounding_box(points)
        area = _polygon_area(points)
        room_box = (bbox["pos_x"], bbox["pos_y"],
                    bbox["pos_x"] + bbox["width"], bbox["pos_y"] + bbox["height"])
        has_ventilation = any(_boxes_overlap(room_box, w) for w in window_boxes)

        rooms.append({
            "room_type": room_type,
            "area_sqm": round(area, 2),
            "width_m": bbox["width"],
            "length_m": bbox["height"],
            "has_natural_light": has_ventilation,  # proxy: a window implies light too
            "has_ventilation": has_ventilation,
        })

    if not components and not rooms:
        warnings.append(
            "No recognized WALL/DOOR/WINDOW/... or ROOM_* layers found. "
            "Check the DXF follows the documented layer-naming convention."
        )

    return {"components": components, "rooms": rooms, "warnings": warnings}


def _boxes_overlap(a: tuple[float, float, float, float], b: tuple[float, float, float, float]) -> bool:
    ax1, ay1, ax2, ay2 = a
    bx1, by1, bx2, by2 = b
    return not (ax2 < bx1 or bx2 < ax1 or ay2 < by1 or by2 < ay1)
