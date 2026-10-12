import { Plus } from "lucide-react";
import { useEffect, useState } from "react";
import { Badge, Button, Table } from "../../components/ui";
import { api } from "../../lib/api";
import { type Order } from "../../types/domain";

export function Orders({ go }: { go: (v: string) => void }) {
  const [items, setItems] = useState<Order[]>([]);
  const [erro, setErro] = useState("");
  useEffect(() => {
    api<Order[]>("/ordens")
      .then(setItems)
      .catch((e: Error) => setErro(e.message));
  }, []);
  return (
    <div className="module-shell space-y-5">
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      <div className="toolbar">
        <div>
          <h2>Atendimentos</h2>
          <p>Controle de serviços, custos e status.</p>
        </div>
        <span className="toolbar-actions">
          <Button variant="secondary" onClick={() => go("quotes-new")}>
            <Plus size={16} aria-hidden="true" /> Novo orçamento
          </Button>
          <Button onClick={() => go("orders-new")}>
            <Plus size={16} aria-hidden="true" /> Nova ordem
          </Button>
        </span>
      </div>
      <Table
        headers={["Atendimento", "Cliente", "Veículo", "KM", "Status", "Ação"]}
        rows={items.map((o) => [
          `${o.status === "ORCAMENTO" ? "Orçamento" : "OS"} #${o.id}`,
          o.cliente.nome,
          `${o.veiculo.marcaModelo} · ${o.veiculo.placa}`,
          String(o.quilometragem),
          <Badge key="b" status={o.status} />,
          <Button
            key="a"
            size="sm"
            variant="link"
            onClick={() => go("order-" + o.id)}
          >
            {o.status === "ORCAMENTO" ? "Ver orçamento" : "Ver OS"}
          </Button>,
        ])}
      />
    </div>
  );
}
