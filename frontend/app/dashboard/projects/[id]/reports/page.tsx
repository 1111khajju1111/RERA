"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { FileText, FileSpreadsheet, File, Download, Loader2 } from "lucide-react";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { reportApi, ApiError } from "@/lib/api";
import type { AuditReportResponse } from "@/lib/api";

const FORMAT_META = {
  PDF: { icon: File, label: "PDF", color: "text-red-400" },
  DOCX: { icon: FileText, label: "Word", color: "text-blue-400" },
  XLSX: { icon: FileSpreadsheet, label: "Excel", color: "text-emerald-400" },
} as const;

export default function ReportsPage() {
  const { id } = useParams<{ id: string }>();
  const projectId = Number(id);

  const [reports, setReports] = useState<AuditReportResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    try {
      setReports(await reportApi.list(projectId));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [projectId]);

  async function handleGenerate(format: "PDF" | "DOCX" | "XLSX") {
    setGenerating(format);
    setError(null);
    try {
      await reportApi.generate(projectId, format);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : `Failed to generate ${format} report`);
    } finally {
      setGenerating(null);
    }
  }

  function handleDownload(reportId: number) {
    // Plain navigation, not fetch — the session cookie goes along automatically
    // and the browser handles the file download/save-as itself.
    window.open(reportApi.downloadUrl(projectId, reportId), "_blank");
  }

  const downloadableReports = reports.filter((r) => r.downloadable);

  return (
    <div>
      <h1 className="mb-1 text-2xl font-semibold">Reports</h1>
      <p className="mb-6 text-sm text-white/40">
        Generate a compliance audit report combining violations, AI suggestions, and site data.
      </p>

      <Card className="mb-6">
        <CardHeader><CardTitle>Generate New Report</CardTitle></CardHeader>
        <div className="flex flex-wrap gap-3">
          {(Object.keys(FORMAT_META) as Array<keyof typeof FORMAT_META>).map((format) => {
            const meta = FORMAT_META[format];
            const Icon = meta.icon;
            const isGenerating = generating === format;
            return (
              <Button
                key={format}
                variant="secondary"
                onClick={() => handleGenerate(format)}
                disabled={generating !== null}
                className="gap-2"
              >
                {isGenerating ? <Loader2 className="h-4 w-4 animate-spin" /> : <Icon className={`h-4 w-4 ${meta.color}`} />}
                {isGenerating ? "Generating…" : `Generate ${meta.label}`}
              </Button>
            );
          })}
        </div>
        {error && <p className="mt-3 text-sm text-red-400">{error}</p>}
      </Card>

      <Card>
        <CardHeader><CardTitle>Report History</CardTitle></CardHeader>
        {loading ? (
          <p className="text-sm text-white/40">Loading…</p>
        ) : downloadableReports.length === 0 ? (
          <p className="text-sm text-white/40">No reports generated yet.</p>
        ) : (
          <div className="space-y-2">
            {downloadableReports.map((r) => {
              const meta = FORMAT_META[r.format as keyof typeof FORMAT_META] || FORMAT_META.PDF;
              const Icon = meta.icon;
              return (
                <div key={r.id} className="flex items-center justify-between rounded-xl border border-border bg-white/[0.02] p-3">
                  <div className="flex items-center gap-3">
                    <Icon className={`h-5 w-5 ${meta.color}`} />
                    <div>
                      <div className="text-sm">
                        {meta.label} Report
                        {r.complianceScore !== null && (
                          <Badge tone="neutral" className="ml-2">Score: {r.complianceScore}</Badge>
                        )}
                      </div>
                      <div className="text-xs text-white/40">
                        {new Date(r.generatedAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                  <Button variant="ghost" size="sm" className="gap-1.5" onClick={() => handleDownload(r.id)}>
                    <Download className="h-3.5 w-3.5" /> Download
                  </Button>
                </div>
              );
            })}
          </div>
        )}
      </Card>
    </div>
  );
}
