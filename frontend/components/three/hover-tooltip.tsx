"use client";

import { Badge, severityTone } from "@/components/ui/badge";
import type { ComponentResponse, ViolationResponse } from "@/lib/types";

export function HoverTooltip({ info }: { info: { component: ComponentResponse; violation?: ViolationResponse } | null }) {
  if (!info) return null;
  const { component, violation } = info;

  return (
    <div className="pointer-events-none absolute bottom-6 left-1/2 z-10 w-80 -translate-x-1/2">
      <div className="glass-card !p-4">
        <div className="mb-2 flex items-center justify-between">
          <span className="text-sm font-medium">{component.componentType.replace("_", " ")}</span>
          {component.confidenceScore !== null && (
            <span className="text-xs text-white/40">
              {(component.confidenceScore * 100).toFixed(0)}% confidence · {component.detectedBy}
            </span>
          )}
        </div>
        <div className="grid grid-cols-2 gap-1 text-xs text-white/50">
          <span>Width: {component.width?.toFixed(2)}m</span>
          <span>Depth: {component.height?.toFixed(2)}m</span>
        </div>
        {violation && (
          <div className="mt-3 border-t border-border pt-3">
            <div className="mb-1"><Badge tone={severityTone(violation.severity)}>{violation.severity}</Badge></div>
            <p className="text-xs text-white/70">{violation.description}</p>
          </div>
        )}
      </div>
    </div>
  );
}
