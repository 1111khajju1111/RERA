# AI RERA Auditor — Frontend (Phase 5)

Next.js 14 (App Router) + TypeScript + Tailwind, dark glassmorphism theme.

## What's built

- Landing page (hero, animated stats, feature cards)
- Auth: register / login (session-cookie based, matches Phase 3 backend)
- Dashboard shell with sidebar
- Projects: list + create
- Project detail: compliance score gauge, stats, violations table
- Upload page: drag-and-drop, polls real project status (no fake timers)
- AI Assistant chat page, backed by the Phase 4 `/chat` endpoint

## What's NOT in this phase (by design — see roadmap)

- **3D Building Viewer** — Phase 6. This phase intentionally skips heavy
  Three.js so the app stays fast; the landing page uses CSS gradient blobs
  instead of "a huge animated 3D city."
- **GIS Dashboard** — Phase 7
- **Reports (PDF/DOCX/XLSX export UI)** — Phase 9
- **Settings / Profile / 404 pages** — not yet built; low priority for a
  demo, add if you have spare time
- Sidebar nav currently only has "Projects" — items for 3D Viewer, GIS,
  Reports, Settings get added to `components/layout/sidebar.tsx` as those
  pages are built in later phases

## Honest caveats

- **This was written without running `npm install` or `next build`** — no
  network access in the build sandbox. All files are hand-written
  TypeScript/TSX and I've sanity-checked structure and imports carefully,
  but you should run a build locally before trusting it fully:
  ```bash
  npm install
  npm run dev
  ```
- The landing page stats (1,240 projects audited, 87% approval success,
  etc.) are **placeholder marketing numbers**, not real data — swap them
  for actual figures once you have them, or clearly label them as
  illustrative if you keep them for the demo.
- The dashboard's auth guard (`app/dashboard/layout.tsx`) checks a
  **localStorage-persisted user object** for UX purposes only (to avoid a
  flash-redirect on refresh) — it is not a security boundary. Real
  authorization is enforced by the backend's session cookie on every API
  call regardless of what the client-side guard shows.

## Setup

```bash
cd frontend
npm install
cp .env.example .env.local   # set NEXT_PUBLIC_API_URL to your backend
npm run dev
```

Requires the Phase 3 backend running (and reachable at
`NEXT_PUBLIC_API_URL`) with CORS configured to allow the frontend's origin
and credentials — Spring Security's default CORS config will need an
explicit `CorsConfigurationSource` bean allowing `http://localhost:3000`
with `allowCredentials(true)`, which is worth adding in Phase 3's
SecurityConfig before you first run this against it.

## Phase 6 — 3D Building Viewer

Real React Three Fiber scene at `/dashboard/projects/[id]/viewer`, built on
actual parsed component geometry (not placeholder shapes).

**What's real:**
- Rotate/zoom/pan (OrbitControls) and a genuine first-person Walkthrough
  mode (Pointer Lock + WASD, not a mocked toggle)
- Floor isolation, per-component-type layer toggles (Wall/Door/Window/
  Column/Stair/Fire Exit/Corridor)
- Exploded view (vertical offset slider) and section cut (a real Three.js
  clipping plane via `gl.localClippingEnabled`, not a visual fake)
- Violation highlighting: pulses red on the *exact* component instance
  (required a small Phase 3 DTO fix — see below), not just "same type,
  same floor"
- Hover tooltips showing detected component type, dimensions, confidence
  score, and detector name, plus the violation description if flagged
- Heatmap-colored floor slabs (green/amber/red by worst open violation
  severity on that floor)

**Known, documented limitations (see comments in `lib/three-geometry.ts`
and `components/three/scene/floor-group.tsx`):**
- **Rooms are not spatially rendered.** The `rooms` table has no position
  columns — only `building_components` (from DXF parsing) has real x/y/
  width/height. Individual room volumes would need fabricated coordinates,
  which I didn't want to fake. If you want true room-level 3D volumes,
  add `pos_x`/`pos_y` (or a `geometry_json`, matching how components
  already store it) to the `rooms` table and re-run the parser — that's a
  real, scoped follow-up, not a big rework.
- **Vertical extrusion is a per-type constant**, not derived from the
  DXF. Plan-view DXFs don't carry Z-height per element; that needs either
  a section drawing convention or real IFC 3D data (still a stub — see
  AI service Phase 4 README).
- **Plumbing/Electrical toggles are shown disabled** in the toolbar,
  labeled "not detected yet" — these component types don't exist in the
  schema or the DXF layer convention. Adding them is schema + parser work,
  not a viewer change.
- Non-axis-aligned (rotated) walls will render as their axis-aligned
  bounding box, since that's what the DXF parser currently computes.

### Backend change made in this phase
`ViolationResponse` was missing `componentId` — the viewer could only
match violations to components by (floor, type), which would incorrectly
highlight every door on a floor if only one was actually non-compliant.
Added `componentId` to the DTO and mapper (Phase 3 files) so highlighting
is precise. **If you already deployed the Phase 3 backend, re-pull it —
this is a real bug fix, not a cosmetic change.**

## Phase 7 — GIS Dashboard

New page at `/dashboard/projects/[id]/gis`: address input -> real geocode
-> Leaflet map showing the plot marker and actual nearby OSM roads
(color-coded: cyan = OSM-tagged width, amber = estimated width), plus a
fire-access compliance card and an honest "not available" encroachment
panel.

Uses `react-leaflet` — same `ssr:false` dynamic import requirement as the
Three.js viewer, since Leaflet also touches `window` at import time.

## Phase 8 — Compliance Engine Refinement (frontend side)

- Violations table now shows **Resolve**/**Waive** buttons on each open
  violation, and collapses resolved/waived ones into a details panel
  (excluded from the compliance score, but visible for audit trail).
- Project page shows a **compliance score trend chart** (recharts) once
  at least 2 analysis runs exist for a project — each `/run-compliance`
  or GIS analysis now leaves a snapshot, so this fills in naturally as
  you iterate on fixing violations and re-uploading.

## Try it

1. Register a new account (or reuse Phase 2's seeded `demo@rera.ai` once
   you set a real password hash for it — the seed's password_hash is a
   placeholder, it won't match any real password)
2. Create a project
3. Upload `ai-service/sample_data/test_floor_plan.dxf` (generate it via
   the Phase 4 script if you haven't already)
4. Watch the project status move through PROCESSING -> AUDITED
5. Check the compliance score and violations
6. Ask the AI Assistant about it
