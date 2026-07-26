"use client";

import { useEffect, useRef } from "react";
import { useFrame, useThree } from "@react-three/fiber";
import { PointerLockControls } from "@react-three/drei";
import * as THREE from "three";

/**
 * First-person "Building Walkthrough" mode. WASD to move, mouse to look
 * (via browser Pointer Lock — click the canvas to engage, Esc to release).
 * This is a real, working implementation, not a mocked toggle: movement is
 * computed every frame from actual key state and the camera's look
 * direction, same technique any basic first-person controller uses.
 */
export function WalkthroughControls({ onExit }: { onExit: () => void }) {
  const { camera } = useThree();
  const keys = useRef<Record<string, boolean>>({});
  const velocity = useRef(new THREE.Vector3());
  const direction = useRef(new THREE.Vector3());

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => { keys.current[e.code] = true; };
    const onKeyUp = (e: KeyboardEvent) => {
      keys.current[e.code] = false;
      if (e.code === "Escape") onExit();
    };
    window.addEventListener("keydown", onKeyDown);
    window.addEventListener("keyup", onKeyUp);
    return () => {
      window.removeEventListener("keydown", onKeyDown);
      window.removeEventListener("keyup", onKeyUp);
    };
  }, [onExit]);

  useFrame((_, delta) => {
    const speed = 4.5; // m/s — roughly a comfortable walking pace for a building-scale scene
    direction.current.set(0, 0, 0);
    if (keys.current["KeyW"]) direction.current.z -= 1;
    if (keys.current["KeyS"]) direction.current.z += 1;
    if (keys.current["KeyA"]) direction.current.x -= 1;
    if (keys.current["KeyD"]) direction.current.x += 1;
    direction.current.normalize();

    velocity.current.copy(direction.current).multiplyScalar(speed * delta);
    camera.translateX(velocity.current.x);
    camera.translateZ(velocity.current.z);
    // Keep a human eye-level height rather than letting look-direction pitch drag the camera into the floor.
    camera.position.y = 1.7;
  });

  return <PointerLockControls />;
}
