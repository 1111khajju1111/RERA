import { HTMLAttributes } from "react";
import { cn } from "@/lib/utils";

type Tone = "critical" | "major" | "minor" | "success" | "neutral";

const toneClasses: Record<Tone, string> = {
  critical: "bg-red-500/10 text-red-400 border-red-500/30",
  major: "bg-amber-500/10 text-amber-400 border-amber-500/30",
  minor: "bg-yellow-500/10 text-yellow-300 border-yellow-500/30",
  success: "bg-emerald-500/10 text-emerald-400 border-emerald-500/30",
  neutral: "bg-white/[0.06] text-white/70 border-white/10",
};

interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  tone?: Tone;
}

export function Badge({ className, tone = "neutral", ...props }: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium",
        toneClasses[tone],
        className
      )}
      {...props}
    />
  );
}

export function severityTone(severity: string): Tone {
  switch (severity) {
    case "CRITICAL": return "critical";
    case "MAJOR": return "major";
    case "MINOR": return "minor";
    default: return "neutral";
  }
}
