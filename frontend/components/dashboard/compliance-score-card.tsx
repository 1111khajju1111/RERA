import { Card } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import type { ComplianceSummaryResponse } from "@/lib/types";

function scoreColor(score: number) {
  if (score >= 80) return "text-emerald-400";
  if (score >= 50) return "text-amber-400";
  return "text-red-400";
}

export function ComplianceScoreCard({ summary }: { summary: ComplianceSummaryResponse }) {
  if (summary.complianceScore === null) {
    return (
      <Card className="text-center text-sm text-white/40">
        No compliance analysis has run yet for this project — upload a drawing to get a score.
      </Card>
    );
  }

  const circumference = 2 * Math.PI * 54;
  const offset = circumference * (1 - summary.complianceScore / 100);

  return (
    <Card className="flex items-center gap-6">
      <div className="relative h-32 w-32 shrink-0">
        <svg className="h-32 w-32 -rotate-90" viewBox="0 0 120 120">
          <circle cx="60" cy="60" r="54" fill="none" stroke="rgba(255,255,255,0.08)" strokeWidth="10" />
          <circle
            cx="60" cy="60" r="54" fill="none" strokeWidth="10" strokeLinecap="round"
            className={cn(scoreColor(summary.complianceScore), "stroke-current transition-all duration-700")}
            strokeDasharray={circumference}
            strokeDashoffset={offset}
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className={cn("text-2xl font-bold", scoreColor(summary.complianceScore))}>
            {summary.complianceScore}
          </span>
          <span className="text-[10px] text-white/40">COMPLIANCE</span>
        </div>
      </div>

      <div className="space-y-2">
        <div>
          <div className="text-xs text-white/40">Approval Probability</div>
          <div className="text-xl font-semibold">{summary.approvalProbability}%</div>
        </div>
        <div className="flex gap-4 text-xs">
          <span className="text-red-400">{summary.criticalViolations} critical</span>
          <span className="text-amber-400">{summary.majorViolations} major</span>
          <span className="text-yellow-300">{summary.minorViolations} minor</span>
        </div>
      </div>
    </Card>
  );
}
