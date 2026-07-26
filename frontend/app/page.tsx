"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import { ScanLine, ShieldCheck, Building2, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { AnimatedCounter } from "@/components/landing/animated-counter";

const stats = [
  { label: "Projects Audited", value: 1240, suffix: "+" },
  { label: "Approval Success", value: 87, suffix: "%" },
  { label: "Avg. Time Saved", value: 68, suffix: "%" },
  { label: "Detection Accuracy", value: 94, suffix: "%" },
];

export default function LandingPage() {
  return (
    <main className="relative min-h-screen overflow-hidden">
      {/* Ambient gradient blobs — a lightweight stand-in for the "huge animated
          3D city" brief; the real 3D digital twin lives in the dashboard's
          3D viewer (Phase 6), which is a better place for heavy Three.js
          than a marketing page that needs to load fast. */}
      <div className="pointer-events-none absolute -top-40 -left-40 h-[500px] w-[500px] rounded-full bg-brand-blue/20 blur-[120px]" />
      <div className="pointer-events-none absolute top-20 -right-40 h-[500px] w-[500px] rounded-full bg-brand-purple/20 blur-[120px]" />

      <nav className="relative z-10 flex items-center justify-between px-8 py-6 md:px-16">
        <div className="flex items-center gap-2 text-lg font-semibold">
          <ScanLine className="h-5 w-5 text-brand-cyan" />
          AI RERA Auditor
        </div>
        <div className="flex items-center gap-3">
          <Link href="/login"><Button variant="ghost" size="sm">Log in</Button></Link>
          <Link href="/register"><Button variant="primary" size="sm">Upload Building</Button></Link>
        </div>
      </nav>

      <section className="relative z-10 mx-auto flex max-w-4xl flex-col items-center px-6 pt-20 text-center md:pt-28">
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="mb-6 inline-flex items-center gap-2 rounded-full border border-border bg-white/[0.03] px-4 py-1.5 text-xs text-white/60"
        >
          <ShieldCheck className="h-3.5 w-3.5 text-brand-cyan" />
          Built for RERA · NBC · Local Bylaw Compliance
        </motion.div>

        <motion.h1
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.1 }}
          className="text-4xl font-bold leading-tight md:text-6xl"
        >
          From Blueprint to Approval —{" "}
          <span className="brand-text-gradient">Powered by Artificial Intelligence</span>
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="mt-6 max-w-2xl text-lg text-white/60"
        >
          Upload a DXF, IFC, or PDF drawing. Get automatic RERA, NBC, and fire-safety
          compliance checks, violation explanations, and a compliance-ready report —
          in minutes, not weeks.
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="mt-10 flex flex-col gap-3 sm:flex-row"
        >
          <Link href="/register">
            <Button size="lg" className="gap-2">
              Upload Building <ArrowRight className="h-4 w-4" />
            </Button>
          </Link>
          <Link href="/login">
            <Button size="lg" variant="secondary">Live Demo</Button>
          </Link>
        </motion.div>
      </section>

      <section className="relative z-10 mx-auto mt-24 grid max-w-5xl grid-cols-2 gap-4 px-6 pb-24 md:grid-cols-4">
        {stats.map((stat, i) => (
          <motion.div
            key={stat.label}
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5, delay: i * 0.08 }}
          >
            <Card className="text-center">
              <div className="text-3xl font-bold brand-text-gradient">
                <AnimatedCounter target={stat.value} suffix={stat.suffix} />
              </div>
              <div className="mt-1 text-sm text-white/50">{stat.label}</div>
            </Card>
          </motion.div>
        ))}
      </section>

      <section className="relative z-10 mx-auto max-w-5xl px-6 pb-32">
        <div className="grid gap-4 md:grid-cols-3">
          {[
            { icon: Building2, title: "Automatic Rule Checking", desc: "FAR, ground coverage, fire exits, room sizes — checked against codified NBC/RERA rules, not guesses." },
            { icon: ScanLine, title: "Explainable Violations", desc: "Every flagged issue cites the exact clause and the measured value that failed it." },
            { icon: ShieldCheck, title: "AI-Assisted Fixes", desc: "Ask the built-in assistant why a building was flagged and how to resolve it." },
          ].map((f) => (
            <Card key={f.title} className="glass-card-hover">
              <f.icon className="mb-3 h-6 w-6 text-brand-cyan" />
              <h3 className="mb-2 font-semibold">{f.title}</h3>
              <p className="text-sm text-white/50">{f.desc}</p>
            </Card>
          ))}
        </div>
      </section>
    </main>
  );
}
