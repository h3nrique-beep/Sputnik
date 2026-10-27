import { useQuery } from "@tanstack/react-query";

import { Table } from "../../components/ui";

import { dateTime } from "../../lib/format";
import { type Movement } from "../../types/domain";

export function MonthlyReport() {
  const { data: items = [], error } = useQuery<Movement[]>({
    queryKey: ["/relatorio-mensal"],
  });
  const erro = error?.message;
  return (
    <>
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      <Table
        headers={[
          "Data",
          "Peça",
          "Tipo",
          "Quantidade",
          "Saldo",
          "Responsável",
          "Observação",
        ]}
        rows={items.map((m) => [
          dateTime(m.data),
          m.peca.nome,
          m.tipo,
          m.quantidade,
          m.saldoResultante,
          m.operador,
          m.observacao || "—",
        ])}
      />
    </>
  );
}
