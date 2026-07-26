"""
STUB — interface defined, not implemented.

IfcOpenShell is not reliably pip-installable across platforms (conda-forge
or a manual platform wheel is typically required), so it's deliberately
left out of requirements.txt rather than silently failing at import time.

To implement:
  1. Add ifcopenshell to your Docker image directly (see IfcOpenShell's
     GitHub releases for prebuilt wheels matching your Python version/OS)
  2. Use ifcopenshell.open(file_path) and iterate
     ifc_file.by_type("IfcWall"), "IfcDoor", "IfcWindow", "IfcSpace", etc.
  3. IFC's own semantic types make this actually easier than DXF's
     layer-convention guessing — IfcSpace elements even carry area
     properties directly in many exports.

Until then, calling parse_ifc raises NotImplementedError rather than
returning empty/fake data, so a caller can't mistake "not built yet" for
"parsed and found nothing."
"""


def parse_ifc(file_path: str) -> dict:
    raise NotImplementedError(
        "IFC parsing is not yet implemented — see module docstring for the plan. "
        "Use a DXF export for now."
    )
