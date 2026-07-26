"use client";

import * as THREE from "three";
import { ComponentMesh } from "./component-mesh";
import { floorBaseY, floorFootprint } from "@/lib/three-geometry";
import type { FloorResponse, ViolationResponse, ComponentResponse } from "@/lib/types";

interface FloorGroupProps {
  floor: FloorResponse;
  visibleTypes: Set<string>;
  violations: ViolationResponse[];
  explodeOffset: number;
  onHover: (info: { component: ComponentResponse; violation?: ViolationResponse } | null) => void;
  clippingPlanes: THREE.Plane[];
}

function violationFor(component: ComponentResponse, violations: ViolationResponse[]): ViolationResponse | undefined {
  // Exact match when the backend told us which component instance failed;
  // this is precise (Phase 6 fix to ViolationResponse.componentId), not a
  // type+floor guess that would over-highlight identical components.
  return violations.find((v) => v.componentId === component.id);
}

function heatmapColor(floorViolations: ViolationResponse[]): string {
  if (floorViolations.some((v) => v.severity === "CRITICAL")) return "#7f1d1d";
  if (floorViolations.some((v) => v.severity === "MAJOR")) return "#78350f";
  if (floorViolations.length > 0) return "#713f12";
  return "#14532d"; // clean floor reads green
}

export function FloorGroup({ floor, visibleTypes, violations, explodeOffset, onHover, clippingPlanes }: FloorGroupProps) {
  const floorY = floorBaseY(floor.floorNumber, floor.floorHeightM) + explodeOffset;
  const visibleComponents = floor.components.filter((c) => visibleTypes.has(c.componentType));
  const footprint = floorFootprint(floor.components);
  const floorViolations = violations.filter((v) => v.floorNumber === floor.floorNumber);

  return (
    <group>
      {/* Floor slab — a heatmap-colored plate sized to the parsed components'
          combined bounding box. Individual rooms are NOT drawn as separate
          volumes here: the `rooms` table has no position data (see
          lib/three-geometry.ts header comment), so this slab is the honest
          spatial representation available from what was actually parsed. */}
      <mesh
        position={[footprint.minX + footprint.sizeX / 2, floorY - 0.05, footprint.minZ + footprint.sizeZ / 2]}
        receiveShadow
      >
        <boxGeometry args={[footprint.sizeX, 0.1, footprint.sizeZ]} />
        <meshStandardMaterial color={heatmapColor(floorViolations)} opacity={0.55} transparent clippingPlanes={clippingPlanes} />
      </mesh>

      {visibleComponents.map((component) => (
        <ComponentMesh
          key={component.id}
          component={component}
          floorY={floorY}
          violation={violationFor(component, violations)}
          onHover={onHover}
          clippingPlanes={clippingPlanes}
        />
      ))}
    </group>
  );
}
