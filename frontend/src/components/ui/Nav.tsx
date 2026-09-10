export function Nav({
  label,
  icon,
  active,
  onClick,
}: {
  label: string;
  icon?: string;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button className={active ? "active" : ""} onClick={onClick}>
      {icon && (
        <i className="nav-icon" aria-hidden="true">
          {icon}
        </i>
      )}
      {label}
    </button>
  );
}
