"use client";

import { Layers, Scissors, Move3d, Footprints, Eye, EyeOff } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { TOGGLEABLE_COMPONENT_TYPES, UNMODELED_COMPONENT_TYPES } from "@/lib/types-three";
import { COMPONENT_STYLES } from "@/lib/three-geometry";
import type { FloorResponse } from "@/lib/types";

interface ViewerToolbarProps {
  floors: FloorResponse[];
  visibleFloors: Set<number>;
  onToggleFloor: (floorNumber: number) => void;
  visibleTypes: Set<string>;
  onToggleType: (type: string) => void;
  explodeAmount: number;
  onExplodeChange: (v: number) => void;
  sectionCutEnabled: boolean;
  onToggleSectionCut: () => void;
  sectionCutPosition: number;
  onSectionCutPositionChange: (v: number) => void;
  walkthroughMode: boolean;
  onToggleWalkthrough: () => void;
}

export function ViewerToolbar({
  floors, visibleFloors, onToggleFloor,
  visibleTypes, onToggleType,
  explodeAmount, onExplodeChange,
  sectionCutEnabled, onToggleSectionCut, sectionCutPosition, onSectionCutPositionChange,
  walkthroughMode, onToggleWalkthrough,
}: ViewerToolbarProps) {
  return (
    <div className="absolute left-4 top-4 z-10 w-72 space-y-3">
      <Card className="!p-4">
        <div className="mb-2 flex items-center gap-2 text-sm font-medium">
          <Footprints className="h-4 w-4 text-brand-cyan" /> Walkthrough
        </div>
        <button
          onClick={onToggleWalkthrough}
          className={`w-full rounded-lg px-3 py-2 text-xs font-medium transition-colors ${
            walkthroughMode ? "bg-brand-gradient text-white" : "bg-white/[0.06] text-white/70 hover:bg-white/[0.1]"
          }`}
        >
          {walkthroughMode ? "Click canvas, WASD to move, Esc to exit" : "Enter First-Person Mode"}
        </button>
      </Card>

      <Card className="!p-4">
        <div className="mb-2 flex items-center gap-2 text-sm font-medium">
          <Layers className="h-4 w-4 text-brand-cyan" /> Floor Isolation
        </div>
        <div className="space-y-1">
          {floors.map((f) => (
            <label key={f.id} className="flex cursor-pointer items-center justify-between rounded-lg px-2 py-1.5 text-xs hover:bg-white/[0.04]">
              <span className="text-white/70">Floor {f.floorNumber}</span>
              <button onClick={() => onToggleFloor(f.floorNumber)}>
                {visibleFloors.has(f.floorNumber)
                  ? <Eye className="h-3.5 w-3.5 text-brand-cyan" />
                  : <EyeOff className="h-3.5 w-3.5 text-white/20" />}
              </button>
            </label>
          ))}
        </div>
      </Card>

      <Card className="!p-4">
        <div className="mb-2 text-sm font-medium">Component Layers</div>
        <div className="space-y-1">
          {TOGGLEABLE_COMPONENT_TYPES.map((type) => (
            <label key={type} className="flex cursor-pointer items-center justify-between rounded-lg px-2 py-1.5 text-xs hover:bg-white/[0.04]">
              <span className="flex items-center gap-2 text-white/70">
                <span className="h-2.5 w-2.5 rounded-sm" style={{ backgroundColor: COMPONENT_STYLES[type].color }} />
                {type.replace("_", " ")}
              </span>
              <input type="checkbox" checked={visibleTypes.has(type)} onChange={() => onToggleType(type)}
                className="accent-brand-cyan" />
            </label>
          ))}
          {UNMODELED_COMPONENT_TYPES.map((type) => (
            <div key={type} className="flex items-center justify-between rounded-lg px-2 py-1.5 text-xs opacity-40">
              <span>{type}</span>
              <Badge tone="neutral" className="!text-[10px]">not detected yet</Badge>
            </div>
          ))}
        </div>
      </Card>

      <Card className="!p-4">
        <div className="mb-2 flex items-center gap-2 text-sm font-medium">
          <Move3d className="h-4 w-4 text-brand-cyan" /> Exploded View
        </div>
        <input
          type="range" min={0} max={4} step={0.1} value={explodeAmount}
          onChange={(e) => onExplodeChange(Number(e.target.value))}
          className="w-full accent-brand-cyan"
        />
      </Card>

      <Card className="!p-4">
        <div className="mb-2 flex items-center gap-2 text-sm font-medium">
          <Scissors className="h-4 w-4 text-brand-cyan" /> Section Cut
        </div>
        <button
          onClick={onToggleSectionCut}
          className={`mb-2 w-full rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
            sectionCutEnabled ? "bg-brand-gradient text-white" : "bg-white/[0.06] text-white/70 hover:bg-white/[0.1]"
          }`}
        >
          {sectionCutEnabled ? "Cut Enabled" : "Enable Cut"}
        </button>
        {sectionCutEnabled && (
          <input
            type="range" min={-10} max={10} step={0.2} value={sectionCutPosition}
            onChange={(e) => onSectionCutPositionChange(Number(e.target.value))}
            className="w-full accent-brand-cyan"
          />
        )}
      </Card>
    </div>
  );
}
