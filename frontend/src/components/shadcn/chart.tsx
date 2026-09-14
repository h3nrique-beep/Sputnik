import { type CSSProperties, type HTMLAttributes } from "react";
import { ResponsiveContainer } from "recharts";
import { cn } from "@/lib/cn";

export type ChartConfig = Record<string, { label: string; color: string }>;

export function ChartContainer({
  config,
  className,
  children,
  ...props
}: HTMLAttributes<HTMLDivElement> & { config: ChartConfig }) {
  const style = Object.entries(config).reduce<Record<string, string>>(
    (result, [key, value]) => {
      result[`--color-${key}`] = value.color;
      return result;
    },
    {},
  );
  return (
    <div
      className={cn(
        "h-[260px] w-full text-xs [&_.recharts-cartesian-axis-tick_value]:fill-slate-500",
        className,
      )}
      style={style as CSSProperties}
      {...props}
    >
      <ResponsiveContainer>{children}</ResponsiveContainer>
    </div>
  );
}
