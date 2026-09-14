import { useEffect, useState } from "react";
import { CarFront, Package, Users } from "lucide-react";
import {
  CardContent,
  CardHeader,
  CardTitle,
  ShadcnCard,
} from "../../components/shadcn/card";
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
  const [tab, setTab] = useState<Tab>(allowClients ? "clientes" : "pecas");
  const [counts, setCounts] = useState({ clientes: 0, veiculos: 0, pecas: 0 });
  useEffect(() => {
    void Promise.all([
      allowClients ? api<Client[]>("/clientes") : Promise.resolve([]),
      allowClients ? api<Vehicle[]>("/veiculos") : Promise.resolve([]),
      allowPieces ? api<Piece[]>("/pecas") : Promise.resolve([]),
    ]).then(([clientes, veiculos, pecas]) =>
      setCounts({
        clientes: clientes.length,
        veiculos: veiculos.length,
        pecas: pecas.length,
      }),
    );
  }, [allowClients, allowPieces]);
  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 rounded-xl border border-slate-200 bg-gradient-to-br from-white to-slate-50 p-6 shadow-sm sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-blue-600">
            Central de cadastros
          </p>
          <h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950">
            Cadastros e catálogo
          </h2>
          <p className="mt-2 max-w-2xl text-sm text-slate-500">
            Clientes, veículos e peças organizados em um único espaço para
            reduzir etapas no atendimento.
          </p>
        </div>
        <TabsList aria-label="Tipo de cadastro" className="w-full sm:w-auto">
          {allowClients && (
            <TabsTrigger
              active={tab === "clientes"}
              onClick={() => setTab("clientes")}
            >
              <Users size={15} className="mr-2" />
              Clientes
            </TabsTrigger>
          )}
          {allowClients && (
            <TabsTrigger
              active={tab === "veiculos"}
              onClick={() => setTab("veiculos")}
            >
              <CarFront size={15} className="mr-2" />
              Veículos
            </TabsTrigger>
          )}
          {allowPieces && (
            <TabsTrigger
              active={tab === "pecas"}
              onClick={() => setTab("pecas")}
            >
              <Package size={15} className="mr-2" />
              Peças
            </TabsTrigger>
          )}
        </TabsList>
      </div>
      <section className="grid gap-4 sm:grid-cols-3">
        {allowClients && (
          <ShadcnCard>
            <CardHeader className="pb-2">
              <p className="text-sm font-medium text-slate-500">
                Clientes cadastrados
              </p>
              <CardTitle className="text-3xl">{counts.clientes}</CardTitle>
            </CardHeader>
            <CardContent className="text-xs text-slate-500">
              Base de contatos para atendimento.
            </CardContent>
          </ShadcnCard>
        )}
        {allowClients && (
          <ShadcnCard>
            <CardHeader className="pb-2">
              <p className="text-sm font-medium text-slate-500">
                Veículos vinculados
              </p>
              <CardTitle className="text-3xl">{counts.veiculos}</CardTitle>
            </CardHeader>
            <CardContent className="text-xs text-slate-500">
              Histórico e OS por veículo.
            </CardContent>
          </ShadcnCard>
        )}
        {allowPieces && (
          <ShadcnCard>
            <CardHeader className="pb-2">
              <p className="text-sm font-medium text-slate-500">
                Peças no catálogo
              </p>
              <CardTitle className="text-3xl">{counts.pecas}</CardTitle>
            </CardHeader>
            <CardContent className="text-xs text-slate-500">
              Estoque, preço e localização.
            </CardContent>
          </ShadcnCard>
        )}
      </section>
      <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        {tab === "clientes" && allowClients && <Clients go={go} />}
        {tab === "veiculos" && allowClients && <Vehicles go={go} />}
        {tab === "pecas" && allowPieces && <Pieces go={go} />}
      </section>
    </div>
  );
}
