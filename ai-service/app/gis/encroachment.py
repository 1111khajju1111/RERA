"""
STUB — honestly not implemented, same pattern as ifc_parser.py.

Encroachment detection means comparing a project's LEGAL plot boundary
(from a government survey/cadastral record) against what's actually
built. Doing this correctly needs:
  1. An authoritative plot boundary polygon — India's cadastral data is
     fragmented across state-level systems (e.g. Bhulekh, Bhu-Naksha,
     DILRMP), most without a public, uniform API. Coverage, format, and
     access policy differ per state.
  2. The built footprint — derivable from the DXF-parsed wall components
     (Phase 4), but only meaningful once compared against #1.

Returning a fabricated "compliant"/"non-compliant" status without real
boundary data would be actively misleading — worse than admitting the
gap. This function always returns NOT_AVAILABLE with the reason, so nothing
downstream (UI, reports, chat) can mistake "not checked" for "passed."

To implement for a specific state: source that state's cadastral API or
manually digitize the survey plan into a GeoJSON polygon per project, then
do a straightforward shapely `plot_polygon.contains(building_footprint)`
check.
"""


def check_encroachment(project_id: int) -> dict:
    return {
        "status": "NOT_AVAILABLE",
        "notes": (
            "Encroachment detection requires an authoritative cadastral/survey "
            "boundary, which isn't available via a uniform public API. "
            "Provide a digitized plot boundary GeoJSON for this project to enable this check."
        ),
    }
