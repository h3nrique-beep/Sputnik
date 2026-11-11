import { type ReactNode, useEffect, useRef, useState } from "react";

export function Table({
  headers,
  rows,
  emptyText = "Nenhum registro encontrado. Cadastre um item ou ajuste sua busca.",
}: {
  headers: string[];
  rows: ReactNode[][];
  emptyText?: string;
}) {
  // Só vira região focável quando há rolagem horizontal para alcançar pelo teclado.
  // shortcut: mede a cada render, não ao redimensionar a janela; trocar por ResizeObserver se fizer falta.
  const regiao = useRef<HTMLDivElement>(null);
  const [rola, setRola] = useState(false);
  useEffect(() => {
    const el = regiao.current;
    if (el) setRola(el.scrollWidth > el.clientWidth);
  });
  return (
    <div
      ref={regiao}
      className="data-table w-full overflow-x-auto"
      {...(rola && {
        tabIndex: 0,
        role: "region",
        "aria-label": `Tabela de ${headers.join(", ")}`,
      })}
    >
      <table className="w-full text-sm">
        <thead className="bg-(--card)">
          <tr>
            {headers.map((header) => (
              <th key={header} scope="col">
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-(--line)">
          {!rows.length && (
            <tr className="table-empty">
              <td colSpan={headers.length}>{emptyText}</td>
            </tr>
          )}
          {rows.map((row, rowIndex) => (
            <tr key={rowIndex}>
              {row.map((cell, cellIndex) => (
                <td key={cellIndex}>{cell}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
