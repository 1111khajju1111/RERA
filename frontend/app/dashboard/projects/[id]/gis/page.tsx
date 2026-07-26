"use client";

import { useEffect, useState, FormEvent } from "react";
import { useParams } from "next/navigation";
import dynamic from "next/dynamic";
import { MapPin, ShieldAlert, ShieldCheck, HelpCircle } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { gisApi, projectApi, ApiError } from "@/lib/api";
import type { SiteAnalysisResponse, ProjectResponse } from "@/lib/types";

// Leaflet reads `window` at import time — same ssr:false requirement as
// the Three.js viewer (Phase 6), and for the same reason.
const LeafletMap = dynamic(
  () => import("@/components/gis/leaflet-map").then((m) => m.LeafletMap),
  { ssr: false, loading: () => <div className="flex h-full items-center justify-center text-sm text-white/40">Loading map…</div> }
);

export default function GisPage() {
  const { id } = useParams<{ id: string }>();
  const projectId = Number(id);

  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [site, setSite] = useState<SiteAnalysisResponse | null>(null);
  const [address, setAddress] = useState("");
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    projectApi.get(projectId).then((p) => {
      setProject(p);
      setAddress(p.location || "");
    });
    gisApi.get(projectId).then(setSite).catch(() => setSite(null));
  }, [projectId]);

  async function handleAnalyze(e: FormEvent) {
    e.preventDefault();
    setAnalyzing(true);
    setError(null);
    try {
      const result = await gisApi.analyze(projectId, address);
      setSite(result);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "GIS analysis failed");
    } finally {
      setAnalyzing(false);
    }
  }

  return (
    <div>
      <h1 className="mb-1 text-2xl font-semibold">GIS Dashboard</h1>
      <p className="mb-6 text-sm text-white/40">
        Real geocoding + OpenStreetMap road data. See the note on encroachment detection below.
      </p>

      <Card className="mb-6">
        <form onSubmit={handleAnalyze} className="flex gap-2">
          <Input
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="Enter the project's address"
            className="flex-1"
          />
          <Button type="submit" disabled={analyzing || !address.trim()} className="gap-2">
            <MapPin className="h-4 w-4" /> {analyzing ? "Analyzing…" : "Locate & Analyze"}
          </Button>
        </form>
        {error && <p className="mt-2 text-sm text-red-400">{error}</p>}
      </Card>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="h-[500px] overflow-hidden rounded-2xl border border-border lg:col-span-2">
          {site ? <LeafletMap site={site} /> : (
            <div className="flex h-full items-center justify-center text-sm text-white/40">
              Enter an address above to analyze the site.
            </div>
          )}
        </div>

        <div className="space-y-4">
          <Card>
            <h3 className="mb-3 text-sm font-medium text-white/60">Fire Tender Access</h3>
            {!site || site.nearestRoadDistanceM === null ? (
              <p className="text-xs text-white/40">No road data yet.</p>
            ) : (
              <div className="space-y-2 text-sm">
                <div className="flex items-center gap-2">
                  {site.fireAccessCompliant
                    ? <ShieldCheck className="h-4 w-4 text-emerald-400" />
                    : <ShieldAlert className="h-4 w-4 text-red-400" />}
                  <span className={site.fireAccessCompliant ? "text-emerald-400" : "text-red-400"}>
                    {site.fireAccessCompliant ? "Compliant" : "Non-compliant"}
                  </span>
                </div>
                <div className="text-white/60">
                  Nearest road: <span className="text-white">{site.nearestRoadName || "Unnamed"}</span>
                </div>
                <div className="text-white/60">
                  Distance: <span className="text-white">{site.nearestRoadDistanceM}m</span>
                </div>
                <div className="text-white/60">
                  Width: <span className="text-white">{site.nearestRoadWidthM}m</span>{" "}
                  {site.nearestRoadWidthIsEstimated && (
                    <Badge tone="neutral" className="ml-1">estimated</Badge>
                  )}
                </div>
              </div>
            )}
          </Card>

          <Card>
            <h3 className="mb-3 flex items-center gap-2 text-sm font-medium text-white/60">
              <HelpCircle className="h-4 w-4" /> Encroachment Detection
            </h3>
            <Badge tone="neutral" className="mb-2">{site?.encroachmentStatus || "NOT_AVAILABLE"}</Badge>
            <p className="text-xs text-white/40">
              {site?.encroachmentNotes ||
                "Requires an authoritative cadastral/survey boundary, not available via a uniform public API for most regions."}
            </p>
          </Card>

          <Card>
            <h3 className="mb-2 text-sm font-medium text-white/60">Plot Boundary</h3>
            <p className="text-xs text-white/40">
              Shown as a point (geocoded location), not a survey polygon —
              this tool doesn't have access to your official plot boundary.
              The marker on the map is your reference point for the road/
              distance analysis above.
            </p>
          </Card>
        </div>
      </div>
    </div>
  );
}
