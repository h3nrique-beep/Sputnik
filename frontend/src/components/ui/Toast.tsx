import { CircleCheck, X } from "lucide-react";

export function Toast({
  message,
  onClose,
}: {
  message: string;
  onClose: () => void;
}) {
  return (
    <div className="toast" role="status" aria-live="polite">
      <CircleCheck size={18} aria-hidden="true" />
      <p>{message}</p>
      <button type="button" onClick={onClose} aria-label="Fechar aviso">
        <X size={16} aria-hidden="true" />
      </button>
    </div>
  );
}
