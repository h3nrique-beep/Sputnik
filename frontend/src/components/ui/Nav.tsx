export function Nav({
  label,
  icon,
  active,
  onClick,
}: {
  label: string;
  icon?: ReactNode;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      className={active ? "active" : ""}
      onClick={onClick}
      title={label}
      aria-current={active ? "page" : undefined}
    >
      {icon && (
        <i className="nav-icon" aria-hidden="true">
          {icon}
        </i>
      )}
      <span className="nav-label">{label}</span>
    </button>
  );
}
import { type ReactNode } from "react";
