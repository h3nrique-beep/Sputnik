export function Toast({
  message,
  onClose,
}: {
  message: string;
  onClose: () => void;
}) {
  return (
    <div className="toast" role="status" aria-live="polite">
      <span aria-hidden="true">✓</span>
      <p>{message}</p>
      <button type="button" onClick={onClose} aria-label="Fechar aviso">
        ×
      </button>
    </div>
  );
}
