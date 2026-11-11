import { CatalogHub } from "../features/catalog/CatalogHub";
import { ClientCreate } from "../features/clients/ClientCreate";
import { Dashboard } from "../features/dashboard/Dashboard";
import { DeveloperConsole } from "../features/developer/DeveloperConsole";
import { Finance } from "../features/finance/Finance";
import { PieceCreate } from "../features/inventory/PieceCreate";
import { Pieces } from "../features/inventory/Pieces";
import { Reposition } from "../features/inventory/Reposition";
import { StockMovement } from "../features/inventory/StockMovement";
import { OrderCreate } from "../features/orders/OrderCreate";
import { OrderDetail } from "../features/orders/OrderDetail";
import { Orders } from "../features/orders/Orders";
import { MonthlyReport } from "../features/reports/MonthlyReport";
import { Schedule } from "../features/schedule/Schedule";
import { Users } from "../features/users/Users";
import { VehicleCreate } from "../features/vehicles/VehicleCreate";
import { type User } from "../types/domain";
import { podeAbrir } from "./navigation";

/** Tela da rota atual. Sem permissão não renderiza nada: a casca redireciona. */
export function Views({
  view,
  user,
  go,
  avisar,
  busca,
  pecaMovimento,
  setPecaMovimento,
}: {
  view: string;
  user: User;
  go: (view: string, preserveNotice?: boolean) => void;
  avisar: (mensagem: string) => void;
  busca: string;
  pecaMovimento: string;
  setPecaMovimento: (id: string) => void;
}) {
  if (!podeAbrir(view, user)) return null;
  const allowed = (permission: string) =>
    user.perfil === "ADMIN" || user.permissoes.includes(permission);
  return (
    <>
      {view === "dashboard" && <Dashboard go={go} can={allowed} />}
      {["cadastros", "clients", "vehicles"].includes(view) && (
        <CatalogHub
          go={go}
          tab={view === "vehicles" ? "vehicles" : "clients"}
          initialQuery={busca}
        />
      )}
      {view === "pieces" && (
        <Pieces
          key={busca}
          go={go}
          initialQuery={busca}
          move={(pieceId) => {
            setPecaMovimento(String(pieceId));
            go("movement");
          }}
        />
      )}
      {view === "pieces-new" && (
        <PieceCreate
          done={() => {
            avisar("Peça cadastrada com sucesso.");
            go("pieces", true);
          }}
        />
      )}
      {view === "clients-new" && (
        <ClientCreate
          done={() => {
            avisar("Cliente cadastrado com sucesso.");
            go("clients", true);
          }}
        />
      )}
      {view === "vehicles-new" && (
        <VehicleCreate
          done={() => {
            avisar("Veículo cadastrado e vinculado.");
            go("vehicles", true);
          }}
        />
      )}
      {view === "orders" && <Orders go={go} />}
      {view === "schedule" && <Schedule go={go} />}
      {view === "orders-new" && (
        <OrderCreate
          done={() => {
            avisar("Ordem de serviço aberta.");
            go("orders", true);
          }}
        />
      )}
      {view === "quotes-new" && (
        <OrderCreate
          asQuote
          done={() => {
            avisar(
              "Orçamento criado. Inclua os itens previstos e aprove-o quando o cliente confirmar.",
            );
            go("orders", true);
          }}
        />
      )}
      {view.startsWith("order-") && (
        <OrderDetail
          key={view}
          id={Number(view.substring(6))}
          back={() => go("orders")}
        />
      )}
      {view === "movement" && (
        <StockMovement
          key={pecaMovimento}
          initialPieceId={pecaMovimento}
          done={() => avisar("Movimentação registrada com sucesso.")}
        />
      )}
      {view === "report" && <MonthlyReport />}
      {view === "finance" && <Finance go={go} canOrders={allowed("ORDENS")} />}
      {view === "reposition" && <Reposition />}
      {view === "users" && <Users />}
      {view === "developer" && <DeveloperConsole />}
    </>
  );
}
