export function PermissionFields({
  selected,
  toggle,
}: {
  selected: string[];
  toggle: (value: string) => void;
}) {
  return (
    <fieldset>
      <legend>Módulos permitidos</legend>
      {[
        ["PECAS", "Peças e movimentações"],
        ["ORDENS", "Ordens de serviço"],
        ["RELATORIO_MENSAL", "Relatório mensal"],
        ["REPOSICAO", "Reposição"],
      ].map(([key, label]) => (
        <label className="check" key={key}>
          <input
            type="checkbox"
            checked={selected.includes(key)}
            onChange={() => toggle(key)}
          />
          {label}
        </label>
      ))}
    </fieldset>
  );
}
