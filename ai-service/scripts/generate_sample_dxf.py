"""
Generates a test DXF file that follows the layer-naming convention
documented in app/cad_parsing/layer_mapping.py — so you have something
real to run the parser against without needing an actual architect's file.

Run: python scripts/generate_sample_dxf.py
Output: sample_data/test_floor_plan.dxf
"""

import os
import ezdxf


def build():
    doc = ezdxf.new(setup=True)
    msp = doc.modelspace()

    # Undersized bedroom (8.2 sqm, no window overlap -> fails both
    # min-area and ventilation rules) — mirrors the Phase 2 seed data
    # intentionally, so parser output and seed output are comparable.
    msp.add_lwpolyline(
        [(0, 0), (2.6, 0), (2.6, 3.15), (0, 3.15)], close=True,
        dxfattribs={"layer": "ROOM_BEDROOM"},
    )

    # Compliant living room, with an overlapping window (ventilation passes)
    msp.add_lwpolyline(
        [(3.0, 0), (7.0, 0), (7.0, 4.5), (3.0, 4.5)], close=True,
        dxfattribs={"layer": "ROOM_LIVING"},
    )
    msp.add_lwpolyline(
        [(3.0, 4.5), (4.5, 4.5)], dxfattribs={"layer": "WINDOW"},
    )

    # Compliant kitchen
    msp.add_lwpolyline(
        [(7.5, 0), (10.0, 0), (10.0, 3.0), (7.5, 3.0)], close=True,
        dxfattribs={"layer": "ROOM_KITCHEN"},
    )

    # Undersized fire exit (1.2m wide, fails NBC-FIRE-EXIT-MIN-WIDTH >= 1.5m)
    msp.add_lwpolyline(
        [(0, -0.2), (1.2, -0.2)], dxfattribs={"layer": "FIRE_EXIT"},
    )

    # Compliant staircase (1.35m wide, passes NBC-STAIR-MIN-WIDTH >= 1.2m)
    msp.add_lwpolyline(
        [(11.0, 0), (12.35, 0), (12.35, 4.0), (11.0, 4.0)], close=True,
        dxfattribs={"layer": "STAIR"},
    )

    # A perimeter wall, just so WALL classification has something to catch
    msp.add_line((0, 0), (12.35, 0), dxfattribs={"layer": "WALL"})

    os.makedirs("sample_data", exist_ok=True)
    out_path = "sample_data/test_floor_plan.dxf"
    doc.saveas(out_path)
    print(f"Wrote {out_path}")


if __name__ == "__main__":
    build()
