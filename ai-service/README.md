# AI RERA Auditor — AI Service (Phase 4)

FastAPI microservice: real DXF geometry parsing, a fully deterministic
DB-driven rule engine, and LLM explanations with a data-grounded fallback.

## What's real vs. stubbed — read this before demoing

| Piece | Status |
|---|---|
| DXF parsing (walls, doors, windows, rooms, area calc) | **Real.** Uses `ezdxf`, layer-naming convention (see `app/cad_parsing/layer_mapping.py`) |
| Rule engine (FAR, ground coverage, room area, fire exit/stair/corridor width, ventilation proxy) | **Real.** Fully DB-driven against `compliance_rules`, no hardcoding |
| LLM suggestions & chat | **Real**, via Groq (`openai/gpt-oss-120b`), with automatic fallback to data-grounded templates if `GROQ_API_KEY` isn't set or the API call fails |
| YOLO / SAM2 vision-based component detection | **Not implemented.** No trained model, no annotated CAD dataset exists yet. The layer-convention parser is the honest first pass — see the module docstring in `layer_mapping.py` for what closing this gap would take |
| IFC parsing | **Stub.** Interface defined in `ifc_parser.py`, raises `NotImplementedError` rather than faking output |
| DWG parsing | **Not supported.** Convert to DXF first (e.g. via ODA File Converter) |
| Parking ratio, setback, ramp slope, refuge area rules | **Skipped by the engine** with a documented reason each — not derivable from a single floor-plan DXF without more geometry (site boundary, parking bay symbols, ramp rise/run) |

None of the above is hidden — `evaluate_project()` logs exactly which
parameters it can and can't check, and the response payloads include
`warnings` for anything the parser couldn't classify.

## Setup

```bash
cd ai-service
python -m venv venv && source venv/bin/activate
pip install -r requirements.txt
cp .env.example .env   # fill in DATABASE_URL to match your running Postgres
```

Requires the Phase 2/3 database already migrated (Flyway, via the Spring
Boot backend) — this service reads/writes existing tables, it does not
create them.

## Run

```bash
uvicorn app.main:app --reload --port 8000
```

Swagger docs: http://localhost:8000/docs

## Test end-to-end without the backend or a real architect's file

```bash
# 1. Generate a test DXF that matches the layer convention
python scripts/generate_sample_dxf.py

# 2. Parse it into project_id=1 (use the "Sunrise Residency" project
#    seeded in Phase 2, or create your own project via the backend first)
curl -X POST http://localhost:8000/parse-cad \
  -H "Content-Type: application/json" \
  -d '{"project_id": 1, "file_path": "sample_data/test_floor_plan.dxf"}'

# 3. Run the rule engine against what was just parsed
curl -X POST http://localhost:8000/run-compliance \
  -H "Content-Type: application/json" \
  -d '{"project_id": 1}'

# 4. Generate suggestions for whatever violations were found
curl -X POST http://localhost:8000/generate-suggestions \
  -H "Content-Type: application/json" \
  -d '{"project_id": 1}'

# 5. Ask the chat assistant about it
curl -X POST http://localhost:8000/chat \
  -H "Content-Type: application/json" \
  -d '{"project_id": 1, "message": "Why might this building get rejected?"}'
```

Expect step 2 to report 3 rooms + a few components; step 3 to report
2-3 violations (undersized bedroom, failed ventilation, undersized fire
exit) — matching the intentional defects built into `generate_sample_dxf.py`.

## Phase 7 — GIS Module

New endpoint: `POST /gis/analyze` — real geocoding + real OSM road data,
feeding into the same rule engine and `violations` table as the DXF
pipeline.

| Piece | Status |
|---|---|
| Geocoding | **Real.** Nominatim (OSM), public instance — 1 req/sec, requires a real User-Agent, demo/light-use only (see `app/gis/geocode.py` for the usage policy link) |
| Nearby roads | **Real.** Overpass API, actual OSM ways within 150m, with true point-to-segment distance calculation |
| Road width | **Real when OSM's `width` tag is present** (rare); otherwise a **documented estimate** by highway classification — flagged via `width_is_estimated` in every response, never presented as a measurement |
| Fire tender access compliance | **Real rule evaluation** against the road width/distance data, feeding real `violations` rows (severity CRITICAL) |
| Encroachment detection | **Not implemented — by design.** No uniform public cadastral API exists for India (fragmented per-state systems). Returns `NOT_AVAILABLE` with the reason rather than a fabricated pass/fail — see `app/gis/encroachment.py` |
| Plot boundary | **A point, not a polygon.** The geocoded location is a reference marker, not your legal survey boundary |

Production note: both Nominatim and Overpass's public instances are
fair-use only. Before real usage volume, self-host both (Nominatim and
Overpass both support Docker self-hosting) or switch to a paid geocoder.

## Phase 8 — Compliance Engine Refinement

Three real fixes, not new surface area:

1. **Rule-specific severity.** `compliance_rules.default_severity` (new
   column, V6 migration) replaces the old category-based inference (all
   FIRE rules were automatically CRITICAL regardless of the specific
   rule). `severity_for_rule()` now reads this column, falling back to
   the old category mapping only if a rule's severity wasn't set.
2. **Violation lifecycle.** `PATCH /api/projects/{id}/violations/{id}/status`
   (backend) lets an architect mark a violation RESOLVED or WAIVED, with
   an optional note. The `status` column existed since Phase 3 but
   nothing ever wrote to it — there was no endpoint. Only OPEN violations
   count toward the compliance score now.
3. **Single source of truth for scoring.** The compliance score formula
   was previously implemented independently in the Java backend
   (recomputed live on every GET) with no persistence — `audit_reports`
   sat empty since Phase 3. Now `app/rules_engine/scoring.py` computes it
   once, right after each `/run-compliance` or `/gis/analyze` run, and
   writes a snapshot. The backend's `ComplianceService` just reads the
   latest snapshot instead of duplicating the formula — one place to
   tune it, and a free byproduct: `GET /api/projects/{id}/compliance-history`
   now returns real trend data, rendered as a line chart on the project page.

**If you already deployed Phase 3-7 backend/AI-service builds, re-pull
both** — this phase touches the schema (V6 migration) and changes what
`ComplianceService` reads from, not just additive features.

## Preparing a real architect's DXF

Your source DXF needs layers named to match the convention in
`app/cad_parsing/layer_mapping.py` (`WALL`, `DOOR`, `WINDOW`, `ROOM_BEDROOM`,
etc.). Most real-world files won't follow this out of the box — either
have your CAD source rename layers before export, or treat this as the
known gap that a trained vision model (Phase 4b, roadmap) is meant to
remove entirely.
