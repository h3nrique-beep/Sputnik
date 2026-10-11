import { useEffect, useLayoutEffect, useState } from "react";
import {
  BarChart3,
  CalendarDays,
  CircleDollarSign,
  ChevronLeft,
  ClipboardList,
  Cog,
  LayoutDashboard,
  Moon,
  LogOut,
  Menu,
  Package,
  PackageSearch,
  RefreshCw,
  Settings,
  Sun,
  X,
  UsersRound,
  Wrench,
} from "lucide-react";
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
import { pathForView, subtitle, title, viewFromPath } from "./navigation";

const MENU_RECOLHIDO_STORAGE_KEY = "sputnik.menu-recolhido";
const TEMA_STORAGE_KEY = "sputnik.tema-escuro";

export function App() {
  const [user, setUser] = useState<User | null>(null);
  const [view, setView] = useState(
    () => viewFromPath(window.location.pathname) ?? "dashboard",
  );
  const [menuRecolhido, setMenuRecolhido] = useState(
    () => window.localStorage.getItem(MENU_RECOLHIDO_STORAGE_KEY) === "true",
  );
  const [dark, setDark] = useState(
    () => window.localStorage.getItem(TEMA_STORAGE_KEY) !== "light",
  );
  const [notice, setNotice] = useState("");
  const [mobileMenu, setMobileMenu] = useState(false);
  useEffect(() => {
    if (!mobileMenu) return;
    const previousFocus = document.activeElement as HTMLElement | null;
    const menu = document.getElementById("app-navigation");
    const main = document.getElementById("main-content");
    main?.setAttribute("inert", "");
    menu?.querySelector<HTMLButtonElement>(".mobile-nav-close")?.focus();
    const close = (event: KeyboardEvent) => {
      if (event.key === "Escape") setMobileMenu(false);
    };
    window.addEventListener("keydown", close);
    return () => {
      main?.removeAttribute("inert");
      window.removeEventListener("keydown", close);
      previousFocus?.focus();
    };
  }, [mobileMenu]);
  useLayoutEffect(() => {
    document.documentElement.dataset.theme = user && dark ? "dark" : "light";
    window.localStorage.setItem(TEMA_STORAGE_KEY, dark ? "dark" : "light");
  }, [dark, user]);
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
        const next =
          viewFromPath(window.location.pathname) ?? landingView(account);
        setView(next);
        if (window.location.pathname !== pathForView(next))
          window.history.replaceState({}, "", pathForView(next));
      })
      .catch(() => {
        window.history.replaceState({}, "", "/login");
        setUser(null);
      });
  }, []);
  useEffect(() => {
    const onPopState = () =>
      setView(viewFromPath(window.location.pathname) ?? "dashboard");
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
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
          const next =
            viewFromPath(window.location.pathname) ?? landingView(account);
          setView(next);
          window.history.replaceState({}, "", pathForView(next));
        }}
      />
    );
  if (user.trocaSenhaObrigatoria) {
    window.location.href = "/primeiro-acesso";
    return null;
  }
  const change = (next: string, preserveNotice = false) => {
    if (!preserveNotice) setNotice("");
    setMobileMenu(false);
    window.history.pushState({}, "", pathForView(next));
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
  const menuItems = [
    dashboardAllowed && {
      label: "Visão geral",
      icon: <LayoutDashboard size={18} />,
      active: view === "dashboard",
      view: "dashboard",
    },
    allowed("PECAS") && {
      label: "Estoque de peças",
      icon: <Package size={18} />,
      active: view.startsWith("pieces"),
      view: "pieces",
    },
    (allowed("PECAS") || allowed("ORDENS")) && {
      label: "Central de cadastros",
      icon: <PackageSearch size={18} />,
      active:
        view === "cadastros" ||
        view.startsWith("clients") ||
        view.startsWith("vehicles"),
      view: "cadastros",
    },
    allowed("PECAS") && {
      label: "Movimentar estoque",
      icon: <RefreshCw size={18} />,
      active: view === "movement",
      view: "movement",
    },
    allowed("ORDENS") && {
      label: "Ordens de serviço",
      icon: <ClipboardList size={18} />,
      active: view.startsWith("orders") || view.startsWith("order-"),
      view: "orders",
    },
    allowed("ORDENS") && {
      label: "Agenda",
      icon: <CalendarDays size={18} />,
      active: view === "schedule",
      view: "schedule",
    },
    allowed("RELATORIO_MENSAL") && {
      label: "Relatório mensal",
      icon: <BarChart3 size={18} />,
      active: view === "report",
      view: "report",
    },
    allowed("FINANCEIRO") && {
      label: "Financeiro",
      icon: <CircleDollarSign size={18} />,
      active: view === "finance",
      view: "finance",
    },
    allowed("REPOSICAO") && {
      label: "Reposição",
      icon: <Wrench size={18} />,
      active: view === "reposition",
      view: "reposition",
    },
    user.perfil === "ADMIN" && {
      label: "Usuários",
      icon: <UsersRound size={18} />,
      active: view === "users",
      view: "users",
    },
    user.perfil === "ADMIN" && {
      label: "Assinatura",
      icon: <Settings size={18} />,
      active: view === "subscription",
      view: "subscription",
    },
    user.perfil === "ADMIN" && {
      label: "Desenvolvedor",
      icon: <Cog size={18} />,
      active: view === "developer",
      view: "developer",
    },
  ].filter((item) => item !== false);
  return (
    <div
      className={`shell${menuRecolhido ? " menu-recolhido" : ""}${mobileMenu ? " mobile-menu-open" : ""}`}
    >
      <a className="skip-link" href="#main-content">
        Ir para o conteúdo
      </a>
      {mobileMenu && (
        <button
          className="menu-backdrop"
          onClick={() => setMobileMenu(false)}
          aria-label="Fechar navegação"
        />
      )}
      <aside id="app-navigation" aria-label="Navegação principal">
        <div className="brand">
          <img src="/images/logo.jpg" alt="" width="40" height="40" />
          <span>
            <b>SPUTNIK</b>
            <small>{user.empresaNome || "Estoque automotivo"}</small>
          </span>
        </div>
        <button
          type="button"
          className="nav-toggle"
          onClick={alternarMenu}
          aria-label={menuRecolhido ? "Expandir menu" : "Recolher menu"}
          title={menuRecolhido ? "Expandir menu" : "Recolher menu"}
        >
          <ChevronLeft size={18} aria-hidden="true" />
        </button>
        <button
          className="mobile-nav-close"
          onClick={() => setMobileMenu(false)}
          aria-label="Fechar menu"
        >
          <X size={20} />
        </button>
        <nav>
          {menuItems.map((item) => (
            <Nav
              key={item.view}
              label={item.label}
              icon={item.icon}
              active={item.active}
              onClick={() => change(item.view)}
            />
          ))}
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
              window.history.replaceState({}, "", "/login");
              setUser(null);
            }}
            aria-label="Sair do sistema"
            title="Sair do sistema"
          >
            <LogOut size={17} aria-hidden="true" />
          </button>
        </div>
      </aside>
      <main id="main-content" tabIndex={-1}>
        <header>
          <button
            className="mobile-nav-toggle"
            onClick={() => setMobileMenu(true)}
            aria-label="Abrir menu"
            aria-expanded={mobileMenu}
            aria-controls="app-navigation"
          >
            <Menu size={20} />
          </button>
          <div>
            <h1>{title(view)}</h1>
            <p>{subtitle(view)}</p>
          </div>
          <div className="header-actions">
            <button
              type="button"
              className="theme-toggle"
              onClick={() => setDark(!dark)}
              aria-label={dark ? "Ativar modo claro" : "Ativar modo escuro"}
              title={dark ? "Ativar modo claro" : "Ativar modo escuro"}
            >
              {dark ? (
                <Sun size={18} aria-hidden="true" />
              ) : (
                <Moon size={18} aria-hidden="true" />
              )}
            </button>
            <span>
              Olá, <b>{user.nome}</b>
            </span>
          </div>
        </header>
        {notice && <Toast message={notice} onClose={() => setNotice("")} />}
        <section className="content">
          {view === "dashboard" && <Dashboard go={change} can={allowed} />}{" "}
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
              key={view}
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
          {view === "finance" && (
            <Finance go={change} canOrders={allowed("ORDENS")} />
          )}{" "}
          {view === "reposition" && <Reposition />}{" "}
          {view === "users" && <Users />}
          {view === "subscription" && <Subscription />}
          {view === "developer" && <DeveloperConsole />}
        </section>
      </main>
    </div>
  );
}
