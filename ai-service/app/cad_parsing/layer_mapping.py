"""
Layer-name convention this parser expects. This is documented so a real
architect/CAD user can prepare a DXF the parser understands, and so the
sample generator (scripts/generate_sample_dxf.py) produces a matching file.

Component layers (any LINE/LWPOLYLINE on these layers becomes a component):
    WALL, DOOR, WINDOW, COLUMN, STAIR, FIRE_EXIT, CORRIDOR

Room layers (closed LWPOLYLINE on a layer starting with "ROOM_" becomes a
room; the suffix after "ROOM_" is the room_type):
    ROOM_BEDROOM, ROOM_KITCHEN, ROOM_LIVING, ROOM_TOILET, ROOM_REFUGE, ...

Ventilation convention: a room is marked has_ventilation=True if at least
one WINDOW component's bounding box overlaps the room polygon's bounding
box. This is a coarse geometric proxy, not a true window-to-floor-area
ratio — flagged in the rule evaluator where it's used.

This is a real, working first pass — not a placeholder — but it is
layer-convention-based rather than vision-based. A file with different
layer names, or a scanned/rasterized drawing, will not classify correctly.
That gap is exactly what YOLO/SAM2 component detection (roadmap, not yet
trained) is meant to close: recognizing walls/doors/windows from pixels
regardless of how the source file organizes layers.
"""

COMPONENT_LAYERS = {
    "WALL": "WALL",
    "DOOR": "DOOR",
    "WINDOW": "WINDOW",
    "COLUMN": "COLUMN",
    "STAIR": "STAIR",
    "FIRE_EXIT": "FIRE_EXIT",
    "CORRIDOR": "CORRIDOR",
}

ROOM_LAYER_PREFIX = "ROOM_"


def classify_layer(layer_name: str) -> tuple[str, str | None]:
    """
    Returns (kind, type) where kind is 'component', 'room', or 'unknown'.
    """
    upper = layer_name.upper()
    if upper in COMPONENT_LAYERS:
        return "component", COMPONENT_LAYERS[upper]
    if upper.startswith(ROOM_LAYER_PREFIX):
        return "room", upper[len(ROOM_LAYER_PREFIX):]
    return "unknown", None
