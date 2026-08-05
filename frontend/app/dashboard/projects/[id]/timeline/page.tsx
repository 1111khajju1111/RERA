"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { History, FileText, RefreshCw, Loader2 } from "lucide-react";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { versionApi, projectApi, ApiError } from "@/lib/api";
import type { ProjectVersionResponse } from "@/lib/types";

function scoreTone(score: number | null): "success" | "major" | "critical" | "neutral" {
  if (score === null) return "neutral";
  if (score >= 80) return "success";
  if (score >= 50) return "major";
  return "critical";
}

export default function TimelinePage() {
  const { id } = useParams<{ id: string }>();
  const projectId = Number(id);

  const [versions, setVersions] = useState<ProjectVersionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [reanalyzingId, setReanalyzingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    try {
      setVersions(await versionApi.list(projectId));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [projectId]);

  async function handleReanalyze(versionId: number) {
    setReanalyzingId(versionId);
    setError(null);
    try {
      await versionApi.reanalyze(projectId, versionId);
      await pollForCompletion();
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Re-analysis failed to start");
    } finally {
      setReanalyzingId(null);
    }
  }

  // Same real-polling pattern as the upload page — re-analysis runs
  // asynchronously on the backend, so there's no instant result to show;
  // poll actual project status rather than guessing with a fixed delay.
  async function pollForCompletion(attempt = 0) {
    const MAX_ATTEMPTS = 20;
    if (attempt >= MAX_ATTEMPTS) return;
    const updated = await projectApi.get(projectId);
    if (updated.status === "AUDITED" || updated.status === "APPROVED" || updated.status === "PROCESSING_FAILED") {
      return;
    }
    await new Promise((resolve) => setTimeout(resolve, 2000));
    return pollForCompletion(attempt + 1);
  }

  const chartData = [...versions]
    .reverse()
    .filter((v) => v.complianceScore !== null)
    .map((v) => ({ version: `v${v.versionNumber}`, score: v.complianceScore }));

  return (
    <div>
      <div className="mb-6 flex items-center gap-2">
        <History className="h-5 w-5 text-brand-cyan" />
        <h1 className="text-2xl font-semibold">Project Timeline</h1>
      </div>
      <p className="mb-6 text-sm text-white/40">
        Every uploaded revision, in order, with the compliance score it produced.
      </p>

      {error && <p className="mb-4 text-sm text-red-400">{error}</p>}

      {chartData.length >= 2 && (
        <Card className="mb-6">
          <CardHeader><CardTitle>Score by Version</CardTitle></CardHeader>
          <div className="h-48 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                <XAxis dataKey="version" stroke="rgba(255,255,255,0.3)" fontSize={11} tickLine={false} />
                <YAxis domain={[0, 100]} stroke="rgba(255,255,255,0.3)" fontSize={11} tickLine={false} />
                <Tooltip contentStyle={{ background: "#0b0e1a", border: "1px solid rgba(255,255,255,0.1)", borderRadius: 8, fontSize: 12 }} />
                <Line type="monotone" dataKey="score" stroke="#22d3ee" strokeWidth={2} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </Card>
      )}

      <Card>
        <CardHeader><CardTitle>Revisions ({versions.length})</CardTitle></CardHeader>
        {loading ? (
          <p className="text-sm text-white/40">Loading…</p>
        ) : versions.length === 0 ? (
          <p className="text-sm text-white/40">No uploads yet.</p>
        ) : (
          <div className="space-y-3">
            {versions.map((v) => (
              <div key={v.id} className="flex items-center justify-between rounded-xl border border-border bg-white/[0.02] p-4">
                <div className="flex items-center gap-3">
                  <FileText className="h-4 w-4 text-white/30" />
                  <div>
                    <div className="flex items-center gap-2 text-sm">
                      <span className="font-medium">v{v.versionNumber}</span>
                      <span className="text-white/50">{v.originalFilename}</span>
                    </div>
                    <div className="text-xs text-white/30">
                      {new Date(v.uploadedAt).toLocaleString()}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  {v.complianceScore !== null ? (
                    <Badge tone={scoreTone(v.complianceScore)}>
                      Score: {v.complianceScore} · Approval: {v.approvalProbability}%
                    </Badge>
                  ) : (
                    <Badge tone="neutral">Not yet analyzed</Badge>
                  )}
                  <Button
                    variant="ghost" size="sm" className="gap-1.5"
                    disabled={reanalyzingId !== null}
                    onClick={() => handleReanalyze(v.id)}
                  >
                    {reanalyzingId === v.id
                      ? <Loader2 className="h-3.5 w-3.5 animate-spin" />
                      : <RefreshCw className="h-3.5 w-3.5" />}
                    Re-analyze
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      <p className="mt-4 text-xs text-white/20">
        Note: re-analyzing re-runs detection and rule-checking against the
        originally uploaded file — it does not restore that version's
        building geometry as the project's current live state. Only the
        most recently analyzed upload's geometry is ever "live" in the 3D
        viewer and violations list.
      </p>
    </div>
  );
}
