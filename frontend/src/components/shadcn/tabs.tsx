import { type ButtonHTMLAttributes, type HTMLAttributes } from "react";
import { cn } from "@/lib/cn";

export function TabsList({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn("inline-flex h-10 items-center justify-center rounded-lg bg-slate-100 p-1 text-slate-500", className)} {...props} />;
}

export function TabsTrigger({ active, className, ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { active?: boolean }) {
  return <button className={cn("inline-flex h-8 items-center justify-center rounded-md px-3 text-sm font-medium transition-all hover:text-slate-900", active && "bg-white text-slate-950 shadow-sm", className)} {...props} />;
}
