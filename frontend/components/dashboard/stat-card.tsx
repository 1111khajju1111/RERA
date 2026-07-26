import { LucideIcon } from "lucide-react";
import { Card } from "@/components/ui/card";

export function StatCard({
  icon: Icon, label, value, tone = "text-white",
}: { icon: LucideIcon; label: string; value: string | number; tone?: string }) {
  return (
    <Card className="flex items-center gap-4">
      <div className="rounded-xl bg-white/[0.06] p-3">
        <Icon className="h-5 w-5 text-brand-cyan" />
      </div>
      <div>
        <div className={`text-xl font-semibold ${tone}`}>{value}</div>
        <div className="text-xs text-white/40">{label}</div>
      </div>
    </Card>
  );
}
