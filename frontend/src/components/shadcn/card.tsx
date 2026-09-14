import { type HTMLAttributes, forwardRef } from "react";
import { cn } from "@/lib/cn";

export const ShadcnCard = forwardRef<
  HTMLDivElement,
  HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "rounded-xl border border-slate-200 bg-white text-slate-950 shadow-sm",
      className,
    )}
    {...props}
  />
));
ShadcnCard.displayName = "ShadcnCard";

export const CardHeader = ({
  className,
  ...props
}: HTMLAttributes<HTMLDivElement>) => (
  <div className={cn("flex flex-col gap-1.5 p-5", className)} {...props} />
);
export const CardTitle = ({
  className,
  ...props
}: HTMLAttributes<HTMLHeadingElement>) => (
  <h2
    className={cn("font-semibold leading-none tracking-tight", className)}
    {...props}
  />
);
export const CardContent = ({
  className,
  ...props
}: HTMLAttributes<HTMLDivElement>) => (
  <div className={cn("px-5 pb-5", className)} {...props} />
);
