"use client";

import { useMemo, useState } from "react";
import { Canvas } from "@react-three/fiber";
import { OrbitControls, Grid } from "@react-three/drei";
import * as THREE from "three";

import { FloorGroup } from "./scene/floor-group";
import { WalkthroughControls } from "./controls/walkthrough-controls";
import { ViewerToolbar } from "./viewer-toolbar";
import { HoverTooltip } from "./hover-tooltip";
import { TOGGLEABLE_COMPONENT_TYPES } from "@/lib/types-three";
import type { BuildingResponse, ComponentResponse, ViolationResponse } from "@/lib/types";

interface BuildingViewerProps {
  building: BuildingResponse;
  violations: ViolationResponse[];
}

export function BuildingViewer({ building, violations }: BuildingViewerProps) {
  const [visibleFloors, setVisibleFloors] = useState<Set<number>>(
    new Set(building.floors.map((f) => f.floorNumber))
  );
  const [visibleTypes, setVisibleTypes] = useState<Set<string>>(new Set(TOGGLEABLE_COMPONENT_TYPES));
  const [explodeAmount, setExplodeAmount] = useState(0);
  const [sectionCutEnabled, setSectionCutEnabled] = useState(false);
  const [sectionCutPosition, setSectionCutPosition] = useState(0);
  const [walkthroughMode, setWalkthroughMode] = useState(false);
  const [hoverInfo, setHoverInfo] = useState<{ component: ComponentResponse; violation?: ViolationResponse } | null>(null);

  const clippingPlanes = useMemo(() => {
    if (!sectionCutEnabled) return [];
    // Cuts along the X axis — everything with x > sectionCutPosition is clipped away.
    return [new THREE.Plane(new THREE.Vector3(-1, 0, 0), sectionCutPosition)];
  }, [sectionCutEnabled, sectionCutPosition]);

  function toggleFloor(floorNumber: number) {
    setVisibleFloors((prev) => {
      const next = new Set(prev);
      next.has(floorNumber) ? next.delete(floorNumber) : next.add(floorNumber);
      return next;
    });
  }

  function toggleType(type: string) {
    setVisibleTypes((prev) => {
      const next = new Set(prev);
      next.has(type) ? next.delete(type) : next.add(type);
      return next;
    });
  }

  const sortedFloors = [...building.floors].sort((a, b) => a.floorNumber - b.floorNumber);

  return (
    <div className="relative h-full w-full overflow-hidden rounded-2xl border border-border bg-black">
      <ViewerToolbar
        floors={sortedFloors}
        visibleFloors={visibleFloors} onToggleFloor={toggleFloor}
        visibleTypes={visibleTypes} onToggleType={toggleType}
        explodeAmount={explodeAmount} onExplodeChange={setExplodeAmount}
        sectionCutEnabled={sectionCutEnabled} onToggleSectionCut={() => setSectionCutEnabled((v) => !v)}
        sectionCutPosition={sectionCutPosition} onSectionCutPositionChange={setSectionCutPosition}
        walkthroughMode={walkthroughMode} onToggleWalkthrough={() => setWalkthroughMode((v) => !v)}
      />

      <HoverTooltip info={hoverInfo} />

      <Canvas
        shadows
        camera={{ position: [15, 12, 15], fov: 50 }}
        gl={{ localClippingEnabled: true }}
      >
        <ambientLight intensity={0.4} />
        <directionalLight position={[10, 20, 10]} intensity={1} castShadow />
        <hemisphereLight intensity={0.3} groundColor="#111827" />

        <Grid args={[100, 100]} position={[0, -0.1, 0]} cellColor="#1f2937" sectionColor="#374151" fadeDistance={60} />

        {sortedFloors
          .filter((f) => visibleFloors.has(f.floorNumber))
          .map((floor) => (
            <FloorGroup
              key={floor.id}
              floor={floor}
              visibleTypes={visibleTypes}
              violations={violations}
              explodeOffset={explodeAmount * floor.floorNumber}
              onHover={setHoverInfo}
              clippingPlanes={clippingPlanes}
            />
          ))}

        {walkthroughMode
          ? <WalkthroughControls onExit={() => setWalkthroughMode(false)} />
          : <OrbitControls makeDefault enableDamping dampingFactor={0.08} />}
      </Canvas>
    </div>
  );
}
