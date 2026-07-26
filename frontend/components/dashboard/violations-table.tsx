"use client";

import { useState } from "react";
import { AlertTriangle, CheckCircle2, ShieldOff, Loader2 } from "lucide-react";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge, severityTone } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { violationApi } from "@/lib/api";
import type { ViolationResponse } from "@/lib/types";

interface ViolationsTableProps {
  violations: ViolationResponse[];
  projectId: number;
  onStatusChange?: (violation: ViolationResponse) => void;
}

export function ViolationsTable({ violations, projectId, onStatusChange }: ViolationsTableProps) {
  const [updatingId, setUpdatingId] = useState<number | null>(null);

  async function handleStatusChange(violation: ViolationResponse, status: "RESOLVED" | "WAIVED") {
    setUpdatingId(violation.id);
    try {
      const note = status === "WAIVED"
        ? window.prompt("Optional: why is this being waived? (e.g. \"local authority granted a variance\")") || undefined
        : undefined;
      const updated = await violationApi.updateStatus(projectId, violation.id, status, note);
      onStatusChange?.(updated);
    } finally {
      setUpdatingId(null);
    }
  }

  const openViolations = violations.filter((v) => v.status === "OPEN");
  const resolvedOrWaived = violations.filter((v) => v.status !== "OPEN");

  if (violations.length === 0) {
    return (
      <Card>
        <CardHeader><CardTitle>Violations</CardTitle></CardHeader>
        <p className="text-sm text-white/40">No open violations. This project is currently clean.</p>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader><CardTitle>Violations ({openViolations.length} open)</CardTitle></CardHeader>
      <div className="space-y-3">
        {openViolations.map((v) => (
          <div key={v.id} className="flex items-start gap-3 rounded-xl border border-border bg-white/[0.02] p-4">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-white/30" />
            <div className="flex-1">
              <div className="mb-1 flex items-center gap-2">
                <Badge tone={severityTone(v.severity)}>{v.severity}</Badge>
                <span className="text-xs text-white/40">{v.ruleCode}</span>
                {v.floorNumber !== null && (
                  <span className="text-xs text-white/30">· Floor {v.floorNumber}</span>
                )}
              </div>
              <p className="text-sm text-white/80">{v.description}</p>
              {v.detectedValue !== null && v.requiredValue !== null && (
                <p className="mt-1 text-xs text-white/40">
                  Detected: {v.detectedValue}{v.unit} · Required: {v.requiredValue}{v.unit}
                </p>
              )}
            </div>
            <div className="flex shrink-0 gap-1.5">
              {updatingId === v.id ? (
                <Loader2 className="h-4 w-4 animate-spin text-white/40" />
              ) : (
                <>
                  <Button variant="secondary" size="sm" className="gap-1.5"
                    onClick={() => handleStatusChange(v, "RESOLVED")} title="Mark as fixed">
                    <CheckCircle2 className="h-3.5 w-3.5" /> Resolve
                  </Button>
                  <Button variant="ghost" size="sm" className="gap-1.5"
                    onClick={() => handleStatusChange(v, "WAIVED")} title="Accept as-is (e.g. variance granted)">
                    <ShieldOff className="h-3.5 w-3.5" /> Waive
                  </Button>
                </>
              )}
            </div>
          </div>
        ))}

        {resolvedOrWaived.length > 0 && (
          <details className="rounded-xl border border-border bg-white/[0.01] p-3">
            <summary className="cursor-pointer text-xs text-white/40">
              {resolvedOrWaived.length} resolved/waived (excluded from compliance score)
            </summary>
            <div className="mt-2 space-y-2">
              {resolvedOrWaived.map((v) => (
                <div key={v.id} className="rounded-lg bg-white/[0.02] p-2.5 text-xs">
                  <div className="mb-1 flex items-center gap-2">
                    <Badge tone={v.status === "RESOLVED" ? "success" : "neutral"}>{v.status}</Badge>
                    <span className="text-white/40">{v.ruleCode}</span>
                  </div>
                  <p className="text-white/50">{v.description}</p>
                  {v.resolutionNote && <p className="mt-1 italic text-white/30">"{v.resolutionNote}"</p>}
                </div>
              ))}
            </div>
          </details>
        )}
      </div>
    </Card>
  );
}
