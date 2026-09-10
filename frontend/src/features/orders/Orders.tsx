import { useEffect, useState } from "react";
import { Badge, Button, Table } from "../../components/ui";
import { api } from "../../lib/api";
import { type Order } from "../../types/domain";

export function Orders({ go }: { go: (v: string) => void }) {
  const [items, setItems] = useState<Order[]>([]);
  useEffect(() => {
    api<Order[]>("/ordens").then(setItems);
  }, []);
  return (
    <div className="module-shell space-y-5">
      <div className="toolbar">
        <div>
          <h2>Atendimentos</h2>
          <p>Controle de serviços, custos e status.</p>
        </div>
        <Button onClick={() => go("orders-new")}>
          ＋ Nova ordem
        </Button>
      </div>
      <Table
        headers={["OS", "Cliente", "Veículo", "KM", "Status", "Ação"]}
        rows={items.map((o) => [
          `#${o.id}`,
          o.cliente.nome,
          `${o.veiculo.marcaModelo} · ${o.veiculo.placa}`,
          String(o.quilometragem),
          <Badge key="b" status={o.status} />,
          <Button key="a" size="sm" variant="link" onClick={() => go("order-" + o.id)}>
            Ver OS
          </Button>,
        ])}
      />
    </div>
  );
}
