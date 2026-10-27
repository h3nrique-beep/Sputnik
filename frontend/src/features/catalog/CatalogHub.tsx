import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { CarFront, Package, Users } from "lucide-react";
import { TabsList, TabsTrigger } from "../../components/shadcn/tabs";
import { type Client, type Piece, type Vehicle } from "../../types/domain";

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
  const clientes = useQuery<Client[]>({
    queryKey: ["/clientes"],
    enabled: allowClients,
  });
  const veiculos = useQuery<Vehicle[]>({
    queryKey: ["/veiculos"],
    enabled: allowClients,
  });
  const pecas = useQuery<Piece[]>({
    queryKey: ["/pecas"],
    enabled: allowPieces,
  });
  // Consulta desabilitada fica "pending" para sempre; só as liberadas contam.
  const carregando =
    (allowClients && (clientes.isPending || veiculos.isPending)) ||
    (allowPieces && pecas.isPending);
  const counts = carregando
    ? undefined
    : {
        clientes: clientes.data?.length ?? 0,
        veiculos: veiculos.data?.length ?? 0,
        pecas: pecas.data?.length ?? 0,
      };
  const erro = (clientes.error ?? veiculos.error ?? pecas.error)?.message ?? "";
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
