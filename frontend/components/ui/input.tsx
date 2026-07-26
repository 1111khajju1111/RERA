import { InputHTMLAttributes, forwardRef } from "react";
import { cn } from "@/lib/utils";

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...props }, ref) => (
    <input
      ref={ref}
      className={cn(
        "flex h-11 w-full rounded-xl border border-border bg-white/[0.03] px-4 text-sm text-white placeholder:text-white/30",
        "focus:outline-none focus:ring-2 focus:ring-brand-blue/50 focus:border-brand-blue/50",
        className
      )}
      {...props}
    />
  )
);
Input.displayName = "Input";
