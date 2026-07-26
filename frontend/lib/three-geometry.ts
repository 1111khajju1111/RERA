/**
 * Maps our DB component records into Three.js scene geometry.
 *
 * IMPORTANT: component.width / component.height in the database are the
 * DXF bounding box's X-extent and Y-extent in PLAN VIEW (see
 * ai-service/app/cad_parsing/dxf_parser.py::_bounding_box). They are NOT
 * a vertical/elevation height. Using them as vertical extrusion would be
 * wrong — a 1.2m-wide door would render 1.2m tall instead of ~1.2m wide.
 *
 * So: plan position/footprint comes from the DB (real, parsed geometry).
 * Vertical extrusion comes from sensible per-type constants below (not
 * derived from any DXF data, since 2D floor plans don't carry Z-height
 * per element — that would require a section/elevation drawing or IFC's
 * 3D geometry, which is why ifc_parser.py is flagged as the eventual
 * source of true 3D extents).
 *
 * Coordinate mapping: DXF (x, y) -> Three.js (x, z) horizontal plane.
 * Floor number * floor height -> Three.js y (vertical stacking).
 */

import type { ComponentTypeStyle } from "./types-three";
import type { ComponentResponse } from "./types";

const DEFAULT_FLOOR_HEIGHT_M = 3.2;

export const COMPONENT_STYLES: Record<string, ComponentTypeStyle> = {
  WALL:      { verticalHeight: DEFAULT_FLOOR_HEIGHT_M, yStart: 0,    color: "#8b8f9c", opacity: 1 },
  COLUMN:    { verticalHeight: DEFAULT_FLOOR_HEIGHT_M, yStart: 0,    color: "#6b7280", opacity: 1 },
  STAIR:     { verticalHeight: DEFAULT_FLOOR_HEIGHT_M, yStart: 0,    color: "#a855f7", opacity: 0.85 },
  DOOR:      { verticalHeight: 2.1,                    yStart: 0,    color: "#d97706", opacity: 0.9 },
  FIRE_EXIT: { verticalHeight: 2.1,                    yStart: 0,    color: "#f97316", opacity: 0.9 },
  WINDOW:    { verticalHeight: 1.2,                    yStart: 0.9,  color: "#22d3ee", opacity: 0.4 },
  CORRIDOR:  { verticalHeight: 0.05,                   yStart: 0,    color: "#3f3f46", opacity: 0.6 },
  DEFAULT:   { verticalHeight: 1.0,                    yStart: 0,    color: "#71717a", opacity: 0.7 },
};

export function floorBaseY(floorNumber: number, floorHeightM: number | null): number {
  return floorNumber * (floorHeightM || DEFAULT_FLOOR_HEIGHT_M);
}

/** Converts one component's DB record into a Three.js box position + scale. */
export function componentToSceneBox(component: ComponentResponse, floorY: number) {
  const style = COMPONENT_STYLES[component.componentType] || COMPONENT_STYLES.DEFAULT;

  // Guard against zero-width/height entities (e.g. a single-point glitch
  // in a malformed DXF) so we never render a degenerate/invisible mesh
  // that would otherwise look like a silent parsing failure.
  const planWidth = Math.max(component.width ?? 0.1, 0.05);
  const planDepth = Math.max(component.height ?? 0.1, 0.05);
  const posX = component.posX ?? 0;
  const posZ = component.posY ?? 0; // DXF Y -> scene Z

  return {
    position: [
      posX + planWidth / 2,
      floorY + style.yStart + style.verticalHeight / 2,
      posZ + planDepth / 2,
    ] as [number, number, number],
    scale: [planWidth, style.verticalHeight, planDepth] as [number, number, number],
    color: style.color,
    opacity: style.opacity,
  };
}

/** Bounding box across all of a floor's components, used to size the floor slab. */
export function floorFootprint(components: ComponentResponse[]) {
  if (components.length === 0) return { minX: 0, minZ: 0, sizeX: 10, sizeZ: 10 };

  const xs = components.flatMap((c) => [c.posX ?? 0, (c.posX ?? 0) + (c.width ?? 0)]);
  const zs = components.flatMap((c) => [c.posY ?? 0, (c.posY ?? 0) + (c.height ?? 0)]);
  const minX = Math.min(...xs), maxX = Math.max(...xs);
  const minZ = Math.min(...zs), maxZ = Math.max(...zs);

  // Pad slightly so the slab reads as a floor, not a tight shrink-wrap.
  const pad = 0.5;
  return { minX: minX - pad, minZ: minZ - pad, sizeX: (maxX - minX) + pad * 2, sizeZ: (maxZ - minZ) + pad * 2 };
}
