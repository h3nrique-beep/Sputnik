import { useEffect, useState } from "react";
import { Loading, Metric, Table } from "../../components/ui";
import { api } from "../../lib/api";
import { money } from "../../lib/format";

export function Reposition() {
  const [data, setData] = useState<any>(),
    [erro, setErro] = useState("");
  useEffect(() => {
    api<any>("/reposicao")
      .then(setData)
      .catch((e) => setErro(e.message));
  }, []);
  if (erro)
    return (
      <div className="alert error" role="alert">
        {erro}
      </div>
    );
  if (!data) return <Loading />;
  return (
    <div className="module-shell space-y-5">
      <div className="metrics metric-grid-3">
        <Metric label="Itens críticos" value={data.pecas.length} />
        <Metric label="Itens zerados" value={data.zeradas} />
        <Metric label="Custo estimado" value={money(data.custo) as any} />
      </div>
      <Table
        headers={[
          "Código",
          "Peça",
          "Disponível",
          "Mínimo",
          "Sugestão",
          "Custo estimado",
        ]}
        rows={data.pecas.map((r: any) => [
          r.peca.codigo,
          r.peca.nome,
          r.peca.disponivel,
          r.peca.minimo,
          r.sugestao,
          money(r.peca.preco * r.sugestao),
        ])}
      />
    </div>
  );
}
