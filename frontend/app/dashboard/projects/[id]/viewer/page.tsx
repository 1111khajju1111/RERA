"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import dynamic from "next/dynamic";
import { Card } from "@/components/ui/card";
import { buildingApi, violationApi } from "@/lib/api";
import type { BuildingResponse, ViolationResponse } from "@/lib/types";

// Three.js touches `window`/WebGL at module load time, which doesn't exist
// during Next.js server-side rendering — ssr:false is required here, not
// optional. Forgetting this is the #1 cause of "works in dev, crashes on
// build" for R3F scenes in the App Router.
const BuildingViewer = dynamic(
  () => import("@/components/three/building-viewer").then((m) => m.BuildingViewer),
  { ssr: false, loading: () => <ViewerSkeleton /> }
);

function ViewerSkeleton() {
  return (
    <div className="flex h-full w-full items-center justify-center rounded-2xl border border-border bg-black">
      <div className="flex items-center gap-2 text-sm text-white/40">
        <div className="h-4 w-4 animate-spin rounded-full border-2 border-brand-cyan border-t-transparent" />
        Loading 3D scene…
      </div>
    </div>
  );
}

export default function ViewerPage() {
  const { id } = useParams<{ id: string }>();
  const projectId = Number(id);

  const [building, setBuilding] = useState<BuildingResponse | null>(null);
  const [violations, setViolations] = useState<ViolationResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      setLoading(true);
      const [buildings, v] = await Promise.all([
        buildingApi.listForProject(projectId),
        violationApi.listForProject(projectId),
      ]);
      setBuilding(buildings[0] || null);
      setViolations(v);
      setLoading(false);
    }
    load();
  }, [projectId]);

  return (
    <div className="flex h-[calc(100vh-4rem)] flex-col">
      <div className="mb-4">
        <h1 className="text-2xl font-semibold">3D Building Viewer</h1>
        <p className="text-sm text-white/40">
          Drag to rotate · scroll to zoom · right-click drag to pan. Red pulsing elements have open violations.
        </p>
      </div>

      {loading ? (
        <ViewerSkeleton />
      ) : !building ? (
        <Card className="flex flex-1 items-center justify-center text-center text-white/40">
          No building data yet. Upload and process a DXF drawing first.
        </Card>
      ) : (
        <div className="flex-1">
          <BuildingViewer building={building} violations={violations} />
        </div>
      )}
    </div>
  );
}
