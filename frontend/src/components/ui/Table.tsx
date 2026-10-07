import { type ReactNode } from "react";

export function Table({
  headers,
  rows,
  emptyText = "Nenhum registro encontrado. Cadastre um item ou ajuste sua busca.",
}: {
  headers: string[];
  rows: ReactNode[][];
  emptyText?: string;
}) {
  return (
    <div
      className="data-table w-full overflow-x-auto"
      tabIndex={0}
      role="region"
      aria-label={`Tabela de ${headers.join(", ")}`}
    >
      <table className="w-full text-sm">
        <thead className="bg-slate-50">
          <tr>
            {headers.map((header) => (
              <th key={header} scope="col">
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
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
