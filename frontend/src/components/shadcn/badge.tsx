import { cva, type VariantProps } from "class-variance-authority";
import { type HTMLAttributes } from "react";
import { cn } from "@/lib/cn";

const badgeVariants = cva("inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold", { variants: { variant: { default: "border-transparent bg-blue-100 text-blue-700", success: "border-transparent bg-emerald-100 text-emerald-700", warning: "border-transparent bg-amber-100 text-amber-700", destructive: "border-transparent bg-red-100 text-red-700", outline: "border-slate-300 text-slate-700" } }, defaultVariants: { variant: "default" } });
export interface ShadcnBadgeProps extends HTMLAttributes<HTMLSpanElement>, VariantProps<typeof badgeVariants> {}
export function ShadcnBadge({ className, variant, ...props }: ShadcnBadgeProps) { return <span className={cn(badgeVariants({ variant }), className)} {...props} />; }
