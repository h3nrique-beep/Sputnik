import { CarFront, Users } from "lucide-react";
import { Clients } from "../clients/Clients";
import { Vehicles } from "../vehicles/Vehicles";

/** Clientes e veículos em abas. A aba vem da rota: F5 e "voltar" a preservam. */
export function CatalogHub({
  go,
  tab,
  initialQuery = "",
}: {
  go: (view: string) => void;
  tab: "clients" | "vehicles";
  initialQuery?: string;
}) {
  return (
    <div className="module-shell space-y-6">
      <div className="catalog-heading">
        <div>
          <p>Clientes e veículos que apoiam o atendimento.</p>
        </div>
        <span
          className="view-toggle"
          role="group"
          aria-label="Tipo de cadastro"
        >
          <button
            type="button"
            aria-pressed={tab === "clients"}
            onClick={() => go("clients")}
          >
            <Users size={16} aria-hidden="true" /> Clientes
          </button>
          <button
            type="button"
            aria-pressed={tab === "vehicles"}
            onClick={() => go("vehicles")}
          >
            <CarFront size={16} aria-hidden="true" /> Veículos
          </button>
        </span>
      </div>
      {tab === "clients" ? (
        <Clients go={go} />
      ) : (
        <Vehicles key={initialQuery} go={go} initialQuery={initialQuery} />
      )}
    </div>
  );
}
