"use client";

import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import type { ComplianceHistoryEntry } from "@/lib/types";

export function ComplianceTrendChart({ history }: { history: ComplianceHistoryEntry[] }) {
  // Fewer than 2 snapshots means there's no trend to show yet — a single
  // point on a line chart is just noise, so show a plain message instead.
  if (history.length < 2) return null;

  const data = [...history]
    .reverse()
    .map((h) => ({
      date: new Date(h.generatedAt).toLocaleDateString(undefined, { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" }),
      score: h.complianceScore,
    }));

  return (
    <Card>
      <CardHeader><CardTitle>Compliance Score Trend</CardTitle></CardHeader>
      <div className="h-48 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
            <XAxis dataKey="date" stroke="rgba(255,255,255,0.3)" fontSize={11} tickLine={false} />
            <YAxis domain={[0, 100]} stroke="rgba(255,255,255,0.3)" fontSize={11} tickLine={false} />
            <Tooltip contentStyle={{ background: "#0b0e1a", border: "1px solid rgba(255,255,255,0.1)", borderRadius: 8, fontSize: 12 }} />
            <Line type="monotone" dataKey="score" stroke="#22d3ee" strokeWidth={2} dot={{ r: 3 }} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </Card>
  );
}
