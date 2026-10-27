import { useEffect, useState } from "react";
import { Button, Loading, Metric, Table } from "../../components/ui";
import { api, downloadApi } from "../../lib/api";
import { money } from "../../lib/format";

export function Reposition() {
  const [data, setData] = useState<any>(),
    [erro, setErro] = useState(""),
    [baixando, setBaixando] = useState(false);
  const exportar = async () => {
    setBaixando(true);
    try {
      await downloadApi("/reposicao.csv", "relatorio-reposicao.csv");
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setBaixando(false);
    }
  };
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
      <div className="toolbar">
        <div>
          <h2>Itens para comprar</h2>
          <p>Peças abaixo do mínimo, com a quantidade sugerida.</p>
        </div>
        <Button
          variant="secondary"
          onClick={exportar}
          disabled={baixando || !data.pecas.length}
        >
          {baixando ? "Gerando CSV..." : "Exportar CSV"}
        </Button>
      </div>
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
        emptyText="Nenhuma peça abaixo do mínimo. O estoque está em dia."
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
