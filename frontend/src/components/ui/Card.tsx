import { type ReactNode } from "react";

export function Card({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  return (
    <div className="card">
      <div className="card-heading flex flex-col gap-1.5 pb-3">
        <h2 className="text-base leading-none font-semibold tracking-tight">
          {title}
        </h2>
      </div>
      <div>{children}</div>
    </div>
  );
}
