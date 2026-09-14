import { useEffect, useState } from "react";
import { Nav, Toast } from "../components/ui";
import { Login } from "../features/auth/Login";
import { ClientCreate } from "../features/clients/ClientCreate";
import { Clients } from "../features/clients/Clients";
import { Dashboard } from "../features/dashboard/Dashboard";
import { CatalogHub } from "../features/catalog/CatalogHub";
import { DeveloperConsole } from "../features/developer/DeveloperConsole";
import { Finance } from "../features/finance/Finance";
import { PieceCreate } from "../features/inventory/PieceCreate";
import { Pieces } from "../features/inventory/Pieces";
import { Reposition } from "../features/inventory/Reposition";
import { StockMovement } from "../features/inventory/StockMovement";
import { OrderCreate } from "../features/orders/OrderCreate";
import { OrderDetail } from "../features/orders/OrderDetail";
import { Orders } from "../features/orders/Orders";
import { Schedule } from "../features/schedule/Schedule";
import { Subscription } from "../features/subscription/Subscription";
import { MonthlyReport } from "../features/reports/MonthlyReport";
import { Users } from "../features/users/Users";
import { VehicleCreate } from "../features/vehicles/VehicleCreate";
import { Vehicles } from "../features/vehicles/Vehicles";
import { api } from "../lib/api";
import { type User } from "../types/domain";
import { subtitle, title } from "./navigation";

const MENU_RECOLHIDO_STORAGE_KEY = "sputnik.menu-recolhido";

export function App() {
  const [user, setUser] = useState<User | null>(null);
  const [view, setView] = useState("dashboard");
  const [menuRecolhido, setMenuRecolhido] = useState(
    () => window.localStorage.getItem(MENU_RECOLHIDO_STORAGE_KEY) === "true",
  );
  const [notice, setNotice] = useState("");
  const landingView = (account: User) => {
    const acessoOperacional =
      account.perfil === "ADMIN" ||
      ["PECAS", "ORDENS", "RELATORIO_MENSAL", "REPOSICAO"].some((permission) =>
        account.permissoes.includes(permission),
      );
    return !acessoOperacional && account.permissoes.includes("FINANCEIRO")
      ? "finance"
      : "dashboard";
  };
  useEffect(() => {
    api<User>("/auth/session")
      .then((account) => {
        setUser(account);
        setView(landingView(account));
      })
      .catch(() => setUser(null));
  }, []);
  useEffect(() => {
    if (!notice) return;
    const timeout = window.setTimeout(() => setNotice(""), 4500);
    return () => window.clearTimeout(timeout);
  }, [notice]);
  if (!user)
    return (
      <Login
        onLogin={(account) => {
          setUser(account);
          setView(landingView(account));
        }}
      />
    );
  if (user.trocaSenhaObrigatoria) {
    window.location.href = "/primeiro-acesso";
    return null;
  }
  const change = (next: string, preserveNotice = false) => {
    if (!preserveNotice) setNotice("");
    setView(next);
  };
  const alternarMenu = () => {
    setMenuRecolhido((recolhido) => {
      const proximoEstado = !recolhido;
      window.localStorage.setItem(
        MENU_RECOLHIDO_STORAGE_KEY,
        String(proximoEstado),
      );
      return proximoEstado;
    });
  };
  const allowed = (permission: string) =>
    user.perfil === "ADMIN" || user.permissoes.includes(permission);
  const dashboardAllowed = [
    "PECAS",
    "ORDENS",
    "RELATORIO_MENSAL",
    "REPOSICAO",
  ].some(allowed);
  return (
    <div className={`shell${menuRecolhido ? " menu-recolhido" : ""}`}>
      <aside>
        <div className="brand">
          <img src="/images/logo.jpg" />
          <span>
            <b>SPUTNIK</b>
            <small>{user.empresaNome || "Gestão de Oficina"}</small>
          </span>
        </div>
        <button
          type="button"
          className="nav-toggle"
          onClick={alternarMenu}
          aria-label={menuRecolhido ? "Expandir menu" : "Recolher menu"}
          title={menuRecolhido ? "Expandir menu" : "Recolher menu"}
        >
          {menuRecolhido ? "›" : "‹"}
        </button>
        <nav>
          {dashboardAllowed && (
            <Nav
              label="Visão geral"
              icon="◫"
              active={view === "dashboard"}
              onClick={() => change("dashboard")}
            />
          )}
          {(allowed("PECAS") || allowed("ORDENS")) && (
            <Nav
              label="Central de cadastros"
              icon="⌂"
              active={
                view === "cadastros" ||
                view.startsWith("clients") ||
                view.startsWith("vehicles") ||
                view.startsWith("pieces")
              }
              onClick={() => change("cadastros")}
            />
          )}
          {allowed("PECAS") && (
            <Nav
              label="Movimentar estoque"
              icon="⇄"
              active={view === "movement"}
              onClick={() => change("movement")}
            />
          )}
          {allowed("ORDENS") && (
            <Nav
              label="Ordens de serviço"
              icon="☷"
              active={view.startsWith("orders") || view.startsWith("order-")}
              onClick={() => change("orders")}
            />
          )}{" "}
          {allowed("ORDENS") && (
            <Nav
              label="Agenda"
              icon="◷"
              active={view === "schedule"}
              onClick={() => change("schedule")}
            />
          )}{" "}
          {allowed("RELATORIO_MENSAL") && (
            <Nav
              label="Relatório mensal"
              icon="▤"
              active={view === "report"}
              onClick={() => change("report")}
            />
          )}{" "}
          {allowed("FINANCEIRO") && (
            <Nav
              label="Financeiro"
              icon="R$"
              active={view === "finance"}
              onClick={() => change("finance")}
            />
          )}{" "}
          {allowed("REPOSICAO") && (
            <Nav
              label="Reposição"
              icon="!"
              active={view === "reposition"}
              onClick={() => change("reposition")}
            />
          )}{" "}
          {user.perfil === "ADMIN" && (
            <>
              <Nav
                label="Usuários"
                icon="♙"
                active={view === "users"}
                onClick={() => change("users")}
              />
              <Nav
                label="Assinatura"
                icon="◇"
                active={view === "subscription"}
                onClick={() => change("subscription")}
              />
              <Nav
                label="Desenvolvedor"
                icon="⌘"
                active={view === "developer"}
                onClick={() => change("developer")}
              />
            </>
          )}
        </nav>
        <div className="profile">
          <span>{user.nome[0]}</span>
          <div>
            <b>{user.nome}</b>
            <small>
              {user.perfil === "ADMIN" ? "Administrador" : "Balconista"}
            </small>
          </div>
          <button
            onClick={async () => {
              await api("/auth/logout", { method: "POST" });
              setUser(null);
            }}
          >
            ↪
          </button>
        </div>
      </aside>
      <main>
        <header>
          <div>
            <h1>{title(view)}</h1>
            <p>{subtitle(view)}</p>
          </div>
          <span>
            Olá, <b>{user.nome}</b>
          </span>
        </header>
        {notice && <Toast message={notice} onClose={() => setNotice("")} />}
        <section className="content">
          {view === "dashboard" && <Dashboard go={change} />}{" "}
          {view === "cadastros" && (
            <CatalogHub
              go={change}
              allowClients={allowed("ORDENS")}
              allowPieces={allowed("PECAS")}
            />
          )}{" "}
          {view === "pieces" && <Pieces go={change} />}{" "}
          {view === "pieces-new" && (
            <PieceCreate
              done={() => {
                setNotice("Peça cadastrada com sucesso.");
                change("pieces", true);
              }}
            />
          )}{" "}
          {view === "clients" && <Clients go={change} />}{" "}
          {view === "clients-new" && (
            <ClientCreate
              done={() => {
                setNotice("Cliente cadastrado com sucesso.");
                change("clients", true);
              }}
            />
          )}{" "}
          {view === "vehicles" && <Vehicles go={change} />}{" "}
          {view === "vehicles-new" && (
            <VehicleCreate
              done={() => {
                setNotice("Veículo cadastrado e vinculado.");
                change("vehicles", true);
              }}
            />
          )}{" "}
          {view === "orders" && <Orders go={change} />}{" "}
          {view === "schedule" && <Schedule go={change} />}{" "}
          {view === "orders-new" && (
            <OrderCreate
              done={() => {
                setNotice("Ordem de serviço aberta.");
                change("orders", true);
              }}
            />
          )}{" "}
          {view === "quotes-new" && (
            <OrderCreate
              asQuote
              done={() => {
                setNotice(
                  "Orçamento criado. Inclua os itens previstos e aprove-o quando o cliente confirmar.",
                );
                change("orders", true);
              }}
            />
          )}{" "}
          {view.startsWith("order-") && (
            <OrderDetail
              id={Number(view.substring(6))}
              back={() => change("orders")}
            />
          )}{" "}
          {view === "movement" && (
            <StockMovement
              done={() => setNotice("Movimentação registrada com sucesso.")}
            />
          )}{" "}
          {view === "report" && <MonthlyReport />}{" "}
          {view === "finance" && <Finance go={change} />}{" "}
          {view === "reposition" && <Reposition />}{" "}
          {view === "users" && <Users />}
          {view === "subscription" && <Subscription />}
          {view === "developer" && <DeveloperConsole />}
        </section>
      </main>
    </div>
  );
}
