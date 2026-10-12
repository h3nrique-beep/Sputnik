import { useEffect, useState } from "react";
import { Table } from "../../components/ui";
import { api } from "../../lib/api";
import { dateTime } from "../../lib/format";

export function MonthlyReport() {
  const [items, setItems] = useState<any[]>([]),
    [erro, setErro] = useState("");
  useEffect(() => {
    api<any[]>("/relatorio-mensal")
      .then(setItems)
      .catch((e) => setErro(e.message));
  }, []);
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
