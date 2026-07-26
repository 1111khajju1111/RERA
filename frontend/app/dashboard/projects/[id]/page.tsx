"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { Upload, MessageSquare, Layers, Home as HomeIcon, Box, Map, FileText } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { StatCard } from "@/components/dashboard/stat-card";
import { ComplianceScoreCard } from "@/components/dashboard/compliance-score-card";
import { ComplianceTrendChart } from "@/components/dashboard/compliance-trend-chart";
import { ViolationsTable } from "@/components/dashboard/violations-table";
import { projectApi, buildingApi, complianceApi, violationApi } from "@/lib/api";
import type { ProjectResponse, BuildingResponse, ComplianceSummaryResponse, ViolationResponse, ComplianceHistoryEntry } from "@/lib/types";

export default function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>();
  const projectId = Number(id);

  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [buildings, setBuildings] = useState<BuildingResponse[]>([]);
  const [summary, setSummary] = useState<ComplianceSummaryResponse | null>(null);
  const [history, setHistory] = useState<ComplianceHistoryEntry[]>([]);
  const [violations, setViolations] = useState<ViolationResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      setLoading(true);
      try {
        const [p, b, v] = await Promise.all([
          projectApi.get(projectId),
          buildingApi.listForProject(projectId),
          violationApi.listForProject(projectId),
        ]);
        setProject(p);
        setBuildings(b);
        setViolations(v);
        // Compliance summary/history needs at least one analysis run to be
        // meaningful; fetched separately so a failure here doesn't blank
        // the whole page.
        try {
          const [s, h] = await Promise.all([
            complianceApi.getSummary(projectId),
            complianceApi.getHistory(projectId),
          ]);
          setSummary(s);
          setHistory(h);
        } catch {
          setSummary(null);
          setHistory([]);
        }
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [projectId]);

  function handleViolationStatusChange(updated: ViolationResponse) {
    setViolations((prev) => prev.map((v) => (v.id === updated.id ? updated : v)));
  }

  if (loading) return <p className="text-sm text-white/40">Loading project…</p>;
  if (!project) return <p className="text-sm text-red-400">Project not found.</p>;

  const building = buildings[0];
  const totalRooms = building?.floors.reduce((sum, f) => sum + f.rooms.length, 0) ?? 0;
  const totalComponents = building?.floors.reduce((sum, f) => sum + f.components.length, 0) ?? 0;

  return (
    <div>
      <div className="mb-8 flex items-center justify-between">
        <div>
          <div className="mb-1 flex items-center gap-2">
            <h1 className="text-2xl font-semibold">{project.name}</h1>
            <Badge tone="neutral">{project.status}</Badge>
          </div>
          <p className="text-sm text-white/40">{project.location || "No location set"}</p>
        </div>
        <div className="flex gap-2">
          <Link href={`/dashboard/projects/${projectId}/upload`}>
            <Button variant="secondary" className="gap-2"><Upload className="h-4 w-4" /> Upload Drawing</Button>
          </Link>
          <Link href={`/dashboard/projects/${projectId}/viewer`}>
            <Button variant="secondary" className="gap-2"><Box className="h-4 w-4" /> 3D Viewer</Button>
          </Link>
          <Link href={`/dashboard/projects/${projectId}/gis`}>
            <Button variant="secondary" className="gap-2"><Map className="h-4 w-4" /> GIS Dashboard</Button>
          </Link>
          <Link href={`/dashboard/projects/${projectId}/reports`}>
            <Button variant="secondary" className="gap-2"><FileText className="h-4 w-4" /> Reports</Button>
          </Link>
          <Link href={`/dashboard/projects/${projectId}/chat`}>
            <Button className="gap-2"><MessageSquare className="h-4 w-4" /> AI Assistant</Button>
          </Link>
        </div>
      </div>

      {!building ? (
        <Card className="mb-6 text-center text-white/40">
          No building data yet. Upload a DXF drawing to run the AI pipeline.
        </Card>
      ) : (
        <>
          <div className="mb-6 grid gap-4 md:grid-cols-4">
            <StatCard icon={Layers} label="Floors" value={building.numFloors} />
            <StatCard icon={HomeIcon} label="Rooms Detected" value={totalRooms} />
            <StatCard icon={Layers} label="Components Detected" value={totalComponents} />
            <StatCard
              icon={Layers}
              label="FAR"
              value={building.farCalculated ?? "—"}
              tone={building.farCalculated && building.farCalculated > 2.5 ? "text-red-400" : "text-emerald-400"}
            />
          </div>

          {summary && (
            <div className="mb-6 grid gap-4 lg:grid-cols-2">
              <ComplianceScoreCard summary={summary} />
              <ComplianceTrendChart history={history} />
            </div>
          )}
        </>
      )}

      <ViolationsTable violations={violations} projectId={projectId} onStatusChange={handleViolationStatusChange} />
    </div>
  );
}
