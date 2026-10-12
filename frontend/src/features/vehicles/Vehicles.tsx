import { Plus } from "lucide-react";
import { Button, Loading, Table } from "../../components/ui";
import { useSearchList } from "../../hooks/useSearchList";
import { type Vehicle } from "../../types/domain";

export function Vehicles({ go }: { go: (v: string) => void }) {
  const { items, query, setQuery, error, loading } =
    useSearchList<Vehicle>("/veiculos");
  return (
    <div className="module-shell space-y-5">
      <div className="toolbar">
        <input
          aria-label="Buscar veículos"
          placeholder="Buscar por placa ou chassi"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <Button onClick={() => go("vehicles-new")}>
          <Plus size={16} aria-hidden="true" /> Novo veículo
        </Button>
      </div>
      {error && (
        <div className="alert error" role="alert">
          {error}
        </div>
      )}
      {loading ? (
        <Loading />
      ) : (
        !error && (
          <Table
            headers={["Veículo", "Placa", "Chassi", "KM", "Cliente"]}
            rows={items.map((v) => [
              v.marcaModelo,
              v.placa,
              v.chassi,
              String(v.quilometragemAtual ?? "—"),
              v.cliente?.nome || "Não vinculado",
            ])}
          />
        )
      )}
    </div>
  );
}
