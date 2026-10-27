export function Loading() {
  return (
    <div className="loading-state" role="status" aria-live="polite">
      <span className="sr-only">Carregando informações…</span>
      <div className="loading-lines" aria-hidden="true">
        <span />
        <span />
        <span />
        <span />
        <span />
      </div>
    </div>
  );
}
