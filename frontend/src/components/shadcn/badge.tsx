import { cva, type VariantProps } from "class-variance-authority";
import { type HTMLAttributes } from "react";
import { cn } from "@/lib/cn";

const badgeVariants = cva(
  "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold",
  {
    variants: {
      variant: {
        default: "border-transparent bg-(--button-soft) text-(--blue)",
        success: "border-transparent bg-(--success-bg) text-(--green)",
        warning: "border-transparent bg-(--warning-bg) text-(--warning)",
        destructive: "border-transparent bg-(--danger-bg) text-(--danger)",
        outline: "border-(--line) text-(--ink)",
      },
    },
    defaultVariants: { variant: "default" },
  },
);
export interface ShadcnBadgeProps
  extends HTMLAttributes<HTMLSpanElement>, VariantProps<typeof badgeVariants> {}
export function ShadcnBadge({
  className,
  variant,
  ...props
}: ShadcnBadgeProps) {
  return (
    <span className={cn(badgeVariants({ variant }), className)} {...props} />
  );
}
