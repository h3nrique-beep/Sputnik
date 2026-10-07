import { useEffect, useState } from "react";
import { CarFront, Package, Users } from "lucide-react";
import { TabsList, TabsTrigger } from "../../components/shadcn/tabs";
import { type Client, type Piece, type Vehicle } from "../../types/domain";
import { api } from "../../lib/api";
import { Clients } from "../clients/Clients";
import { Pieces } from "../inventory/Pieces";
import { Vehicles } from "../vehicles/Vehicles";

type Tab = "clientes" | "veiculos" | "pecas";

export function CatalogHub({
  go,
  allowClients,
  allowPieces,
}: {
  go: (view: string) => void;
  allowClients: boolean;
  allowPieces: boolean;
}) {
  const [tab, setTab] = useState<Tab>(allowPieces ? "pecas" : "clientes");
  const [counts, setCounts] = useState<{
    clientes: number;
    veiculos: number;
    pecas: number;
  }>();
  const [erro, setErro] = useState("");
  useEffect(() => {
    void Promise.all([
      allowClients ? api<Client[]>("/clientes") : Promise.resolve([]),
      allowClients ? api<Vehicle[]>("/veiculos") : Promise.resolve([]),
      allowPieces ? api<Piece[]>("/pecas") : Promise.resolve([]),
    ])
      .then(([clientes, veiculos, pecas]) =>
        setCounts({
          clientes: clientes.length,
          veiculos: veiculos.length,
          pecas: pecas.length,
        }),
      )
      .catch((e: Error) => setErro(e.message));
  }, [allowClients, allowPieces]);
  return (
    <div className="module-shell space-y-6">
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      <div className="catalog-heading">
        <div>
          <h2>Catálogo e atendimento</h2>
          <p>Encontre as peças e os cadastros que apoiam sua operação.</p>
        </div>
        <TabsList aria-label="Tipo de cadastro" role="group">
          {allowPieces && (
            <TabsTrigger
              active={tab === "pecas"}
              onClick={() => setTab("pecas")}
            >
              <Package size={16} className="mr-2" aria-hidden="true" />
              Peças
            </TabsTrigger>
          )}
          {allowClients && (
            <TabsTrigger
              active={tab === "clientes"}
              onClick={() => setTab("clientes")}
            >
              <Users size={16} className="mr-2" aria-hidden="true" />
              Clientes
            </TabsTrigger>
          )}
          {allowClients && (
            <TabsTrigger
              active={tab === "veiculos"}
              onClick={() => setTab("veiculos")}
            >
              <CarFront size={16} className="mr-2" aria-hidden="true" />
              Veículos
            </TabsTrigger>
          )}
        </TabsList>
      </div>
      <div className="catalog-counts" aria-live="polite">
        {allowPieces && (
          <span>
            Peças <strong>{counts?.pecas ?? "—"}</strong>
          </span>
        )}
        {allowClients && (
          <span>
            Clientes <strong>{counts?.clientes ?? "—"}</strong>
          </span>
        )}
        {allowClients && (
          <span>
            Veículos <strong>{counts?.veiculos ?? "—"}</strong>
          </span>
        )}
      </div>
      {tab === "clientes" && allowClients && <Clients go={go} />}
      {tab === "veiculos" && allowClients && <Vehicles go={go} />}
      {tab === "pecas" && allowPieces && <Pieces go={go} />}
    </div>
  );
}
