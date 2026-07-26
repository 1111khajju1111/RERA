"use client";

import { useEffect, useState, FormEvent } from "react";
import Link from "next/link";
import { motion } from "framer-motion";
import { Plus, Building2, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { projectApi } from "@/lib/api";
import type { ProjectResponse } from "@/lib/types";

const statusTone: Record<string, "success" | "neutral" | "major" | "critical"> = {
  DRAFT: "neutral",
  PROCESSING: "major",
  AUDITED: "success",
  APPROVED: "success",
  REJECTED: "critical",
  PROCESSING_FAILED: "critical",
};

export default function ProjectsPage() {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [name, setName] = useState("");
  const [location, setLocation] = useState("");
  const [plotArea, setPlotArea] = useState("");
  const [creating, setCreating] = useState(false);

  async function loadProjects() {
    setLoading(true);
    try {
      setProjects(await projectApi.list());
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadProjects(); }, []);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setCreating(true);
    try {
      await projectApi.create({
        name,
        location: location || undefined,
        plotAreaSqm: plotArea ? Number(plotArea) : undefined,
      });
      setName(""); setLocation(""); setPlotArea("");
      setShowCreate(false);
      await loadProjects();
    } finally {
      setCreating(false);
    }
  }

  return (
    <div>
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Projects</h1>
          <p className="text-sm text-white/40">Every building you've submitted for audit.</p>
        </div>
        <Button onClick={() => setShowCreate(true)} className="gap-2">
          <Plus className="h-4 w-4" /> New Project
        </Button>
      </div>

      {showCreate && (
        <Card className="mb-6">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="font-medium">New Project</h3>
            <button onClick={() => setShowCreate(false)} className="text-white/40 hover:text-white">
              <X className="h-4 w-4" />
            </button>
          </div>
          <form onSubmit={handleCreate} className="grid gap-4 md:grid-cols-3">
            <div>
              <Label htmlFor="name">Project name</Label>
              <Input id="name" required value={name} onChange={(e) => setName(e.target.value)} placeholder="Sunrise Residency" />
            </div>
            <div>
              <Label htmlFor="location">Location</Label>
              <Input id="location" value={location} onChange={(e) => setLocation(e.target.value)} placeholder="Pune, Maharashtra" />
            </div>
            <div>
              <Label htmlFor="plotArea">Plot area (sqm)</Label>
              <Input id="plotArea" type="number" step="0.01" value={plotArea} onChange={(e) => setPlotArea(e.target.value)} placeholder="500" />
            </div>
            <div className="md:col-span-3">
              <Button type="submit" disabled={creating}>{creating ? "Creating…" : "Create Project"}</Button>
            </div>
          </form>
        </Card>
      )}

      {loading ? (
        <p className="text-sm text-white/40">Loading projects…</p>
      ) : projects.length === 0 ? (
        <Card className="text-center text-white/40">
          No projects yet. Create one to get started.
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {projects.map((p, i) => (
            <motion.div key={p.id} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}>
              <Link href={`/dashboard/projects/${p.id}`}>
                <Card className="glass-card-hover h-full cursor-pointer">
                  <CardContent>
                    <div className="mb-3 flex items-start justify-between">
                      <Building2 className="h-5 w-5 text-brand-cyan" />
                      <Badge tone={statusTone[p.status] || "neutral"}>{p.status}</Badge>
                    </div>
                    <h3 className="mb-1 font-medium">{p.name}</h3>
                    <p className="text-sm text-white/40">{p.location || "No location set"}</p>
                    {p.plotAreaSqm && (
                      <p className="mt-2 text-xs text-white/30">{p.plotAreaSqm} sqm plot</p>
                    )}
                  </CardContent>
                </Card>
              </Link>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
