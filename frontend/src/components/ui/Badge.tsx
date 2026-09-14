import { ShadcnBadge, type ShadcnBadgeProps } from "../shadcn/badge";

export function Badge({ status }: { status: string }) {
  const normalized = status.toUpperCase();
  const variant: ShadcnBadgeProps["variant"] = normalized.includes("CONCL")
    ? "success"
    : normalized.includes("AGUARD")
      ? "warning"
      : normalized.includes("CANCEL")
        ? "destructive"
        : "default";
  return (
    <ShadcnBadge variant={variant}>{status.replaceAll("_", " ")}</ShadcnBadge>
  );
}
