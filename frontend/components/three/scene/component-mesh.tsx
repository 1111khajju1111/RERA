"use client";

import { useRef, useState } from "react";
import { useFrame } from "@react-three/fiber";
import * as THREE from "three";
import { componentToSceneBox } from "@/lib/three-geometry";
import type { ComponentResponse, ViolationResponse } from "@/lib/types";

interface ComponentMeshProps {
  component: ComponentResponse;
  floorY: number;
  violation?: ViolationResponse;
  onHover: (info: { component: ComponentResponse; violation?: ViolationResponse } | null) => void;
  clippingPlanes: THREE.Plane[];
}

export function ComponentMesh({ component, floorY, violation, onHover, clippingPlanes }: ComponentMeshProps) {
  const meshRef = useRef<THREE.Mesh>(null);
  const [hovered, setHovered] = useState(false);
  const { position, scale, color, opacity } = componentToSceneBox(component, floorY);

  // Violations pulse red so a judge's eye goes straight to the flagged
  // element without needing the toolbar's legend explained first.
  useFrame(({ clock }) => {
    if (violation && meshRef.current) {
      const pulse = 0.5 + 0.5 * Math.sin(clock.getElapsedTime() * 3);
      (meshRef.current.material as THREE.MeshStandardMaterial).emissiveIntensity = 0.3 + pulse * 0.7;
    }
  });

  return (
    <mesh
      ref={meshRef}
      position={position}
      scale={scale}
      onPointerOver={(e) => { e.stopPropagation(); setHovered(true); onHover({ component, violation }); }}
      onPointerOut={(e) => { e.stopPropagation(); setHovered(false); onHover(null); }}
    >
      <boxGeometry args={[1, 1, 1]} />
      <meshStandardMaterial
        color={violation ? "#ef4444" : color}
        emissive={violation ? "#ef4444" : "#000000"}
        emissiveIntensity={violation ? 0.3 : 0}
        opacity={hovered ? Math.min(opacity + 0.3, 1) : opacity}
        transparent={opacity < 1}
        clippingPlanes={clippingPlanes}
        wireframe={hovered}
      />
    </mesh>
  );
}
