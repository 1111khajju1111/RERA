"use client";

import { useState, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import { UploadCloud, FileText, CheckCircle2, XCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { uploadFile, projectApi, ApiError } from "@/lib/api";

type UploadState = "idle" | "uploading" | "processing" | "done" | "error";

export default function UploadPage() {
  const { id } = useParams<{ id: string }>();
  const projectId = Number(id);
  const router = useRouter();

  const [file, setFile] = useState<File | null>(null);
  const [dragOver, setDragOver] = useState(false);
  const [state, setState] = useState<UploadState>("idle");
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const onDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    const dropped = e.dataTransfer.files[0];
    if (dropped) setFile(dropped);
  }, []);

  async function handleUpload() {
    if (!file) return;
    setState("uploading");
    setErrorMsg(null);
    try {
      await uploadFile(projectId, file);
      // Backend runs the AI pipeline asynchronously (UploadService.triggerAiPipeline)
      // and returns 202 immediately. Poll project status rather than guessing
      // with a fixed delay — this reflects the actual PROCESSING -> AUDITED
      // (or PROCESSING_FAILED) transition set by the backend.
      setState("processing");
      pollForCompletion();
    } catch (err) {
      setState("error");
      setErrorMsg(err instanceof ApiError ? err.message : "Upload failed");
    }
  }

  async function pollForCompletion(attempt = 0) {
    const MAX_ATTEMPTS = 20; // ~40s at 2s intervals — tune to your AI service's typical latency
    if (attempt >= MAX_ATTEMPTS) {
      setState("error");
      setErrorMsg("Still processing after 40s — check the project page shortly.");
      return;
    }
    const updated = await projectApi.get(projectId);
    if (updated.status === "AUDITED" || updated.status === "APPROVED") {
      setState("done");
      router.push(`/dashboard/projects/${projectId}`);
      return;
    }
    if (updated.status === "PROCESSING_FAILED") {
      setState("error");
      setErrorMsg("AI processing failed — the drawing may not follow the expected layer convention.");
      return;
    }
    setTimeout(() => pollForCompletion(attempt + 1), 2000);
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-2 text-2xl font-semibold">Upload Drawing</h1>
      <p className="mb-8 text-sm text-white/40">
        Accepts DXF (fully supported), IFC and PDF (planned — see AI service README).
      </p>

      <Card>
        <div
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onDrop={onDrop}
          className={`flex flex-col items-center justify-center rounded-xl border-2 border-dashed p-12 text-center transition-colors ${
            dragOver ? "border-brand-cyan bg-brand-cyan/5" : "border-border"
          }`}
        >
          {file ? (
            <>
              <FileText className="mb-3 h-8 w-8 text-brand-cyan" />
              <p className="font-medium">{file.name}</p>
              <p className="text-xs text-white/40">{(file.size / 1024).toFixed(1)} KB</p>
            </>
          ) : (
            <>
              <UploadCloud className="mb-3 h-8 w-8 text-white/30" />
              <p className="mb-1 text-sm text-white/60">Drag and drop your DXF file here</p>
              <p className="text-xs text-white/30">or</p>
            </>
          )}

          <label className="mt-4 cursor-pointer">
            <span className="inline-flex items-center rounded-xl bg-white/[0.06] px-4 py-2 text-sm hover:bg-white/[0.1]">
              Browse files
            </span>
            <input
              type="file"
              accept=".dxf,.ifc,.pdf"
              className="hidden"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
            />
          </label>
        </div>

        {state === "error" && (
          <div className="mt-4 flex items-center gap-2 text-sm text-red-400">
            <XCircle className="h-4 w-4" /> {errorMsg}
          </div>
        )}

        {state === "processing" && (
          <div className="mt-4 flex items-center gap-2 text-sm text-brand-cyan">
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-brand-cyan border-t-transparent" />
            Parsing drawing and running compliance checks…
          </div>
        )}

        {state === "done" && (
          <div className="mt-4 flex items-center gap-2 text-sm text-emerald-400">
            <CheckCircle2 className="h-4 w-4" /> Done — redirecting to project…
          </div>
        )}

        <Button
          className="mt-6 w-full"
          disabled={!file || state === "uploading" || state === "processing"}
          onClick={handleUpload}
        >
          {state === "uploading" ? "Uploading…" : "Upload & Analyze"}
        </Button>
      </Card>
    </div>
  );
}
