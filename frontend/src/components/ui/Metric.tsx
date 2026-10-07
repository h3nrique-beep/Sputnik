import { type ReactNode } from "react";

export function Metric({
  icon,
  label,
  value,
  detail = "",
}: {
  icon?: string;
  label: string;
  value: ReactNode;
  detail?: string;
}) {
  return (
    <article className="metric">
      {icon && <i aria-hidden="true">{icon}</i>}
      <div>
        <small>{label}</small>
        <b>{value}</b>
        {detail && <em>{detail}</em>}
      </div>
    </article>
  );
}
