export function Loading() {
  return (
    <div className="loading-state" role="status" aria-live="polite">
      Carregando informações…
      <div className="loading-lines" aria-hidden="true">
        <span />
        <span />
        <span />
      </div>
    </div>
  );
}
