import { useEffect, useState } from "react";
import { Button, Table } from "../../components/ui";
import { api } from "../../lib/api";
import { type Vehicle } from "../../types/domain";

export function Vehicles({ go }: { go: (v: string) => void }) {
  const [items, setItems] = useState<Vehicle[]>([]);
  const [query, setQuery] = useState("");
  useEffect(() => {
    api<Vehicle[]>("/veiculos?busca=" + encodeURIComponent(query)).then(
      setItems,
    );
  }, [query]);
  return (
    <div className="module-shell space-y-5">
      <div className="toolbar">
        <input
          placeholder="Buscar por placa ou chassi"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <Button onClick={() => go("vehicles-new")}>
          ＋ Novo veículo
        </Button>
      </div>
      <Table
        headers={["Veículo", "Placa", "Chassi", "KM", "Cliente"]}
        rows={items.map((v) => [
          v.marcaModelo,
          v.placa,
          v.chassi,
          String(v.quilometragemAtual || "—"),
          v.cliente?.nome || "Não vinculado",
        ])}
      />
    </div>
  );
}
