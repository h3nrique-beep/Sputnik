import { Fragment, useEffect, useLayoutEffect, useRef, useState } from "react";
import { ChevronLeft, Moon, LogOut, Menu, Search, Sun, X } from "lucide-react";
import { Nav, Toast } from "../components/ui";
import { Login } from "../features/auth/Login";
import { api, queryClient } from "../lib/api";
import { type User } from "../types/domain";
import { itensDoMenu } from "./menu";
import {
  destinoDaBusca,
  pathForView,
  podeAbrir,
  subtitle,
  title,
  viewFromPath,
} from "./navigation";
import { Views } from "./Views";

const MENU_RECOLHIDO_STORAGE_KEY = "sputnik.menu-recolhido";
const TEMA_STORAGE_KEY = "sputnik.tema-escuro";

export function App() {
  const [user, setUser] = useState<User | null>(null);
  // Evita o flash da tela de login no F5 enquanto a sessão ainda é consultada.
  const [verificandoSessao, setVerificandoSessao] = useState(true);
  const [view, setView] = useState(
    () => viewFromPath(window.location.pathname) ?? "dashboard",
  );
  const [menuRecolhido, setMenuRecolhido] = useState(
    () => window.localStorage.getItem(MENU_RECOLHIDO_STORAGE_KEY) === "true",
  );
  const [dark, setDark] = useState(() => {
    // Sem escolha salva, segue o tema do sistema operacional.
    const salvo = window.localStorage.getItem(TEMA_STORAGE_KEY);
    return salvo
      ? salvo !== "light"
      : window.matchMedia("(prefers-color-scheme: dark)").matches;
  });
  const [notice, setNotice] = useState("");
  const [busca, setBusca] = useState("");
  const [buscaGlobal, setBuscaGlobal] = useState("");
  const [pecaMovimento, setPecaMovimento] = useState("");
  const [mobileMenu, setMobileMenu] = useState(false);
  const userRef = useRef(user);
  userRef.current = user;
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
    document.documentElement.dataset.theme =
      (user || verificandoSessao) && dark ? "dark" : "light";
    window.localStorage.setItem(TEMA_STORAGE_KEY, dark ? "dark" : "light");
  }, [dark, user, verificandoSessao]);
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
    // URL de módulo sem permissão leva à tela inicial do usuário.
    if (!user || podeAbrir(view, user)) return;
    const next = landingView(user);
    if (next === view) return;
    window.history.replaceState({}, "", pathForView(next));
    setView(next);
  }, [user, view]);
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
      })
      .finally(() => setVerificandoSessao(false));
  }, []);
  useEffect(() => {
    const onPopState = () =>
      setView(viewFromPath(window.location.pathname) ?? "dashboard");
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, []);
  useEffect(() => {
    // Ctrl+K foca a busca do topo; N abre nova OS fora de campos de texto.
    const atalho = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k") {
        const campo = document.querySelector<HTMLInputElement>(
          ".global-search input",
        );
        if (!campo) return;
        event.preventDefault();
        campo.focus();
        return;
      }
      const alvo = event.target as HTMLElement;
      if (
        event.key.toLowerCase() !== "n" ||
        event.ctrlKey ||
        event.metaKey ||
        event.altKey ||
        alvo.isContentEditable ||
        ["INPUT", "TEXTAREA", "SELECT"].includes(alvo.tagName)
      )
        return;
      const conta = userRef.current;
      if (
        conta &&
        (conta.perfil === "ADMIN" || conta.permissoes.includes("ORDENS"))
      ) {
        event.preventDefault();
        window.history.pushState({}, "", pathForView("orders-new"));
        setView("orders-new");
      }
    };
    window.addEventListener("keydown", atalho);
    return () => window.removeEventListener("keydown", atalho);
  }, []);
  useEffect(() => {
    if (!notice) return;
    const timeout = window.setTimeout(() => setNotice(""), 4500);
    return () => window.clearTimeout(timeout);
  }, [notice]);
  if (!user && verificandoSessao) return null;
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
  const menuItems = itensDoMenu(view, user);
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
          {menuItems.map((item, index) => (
            <Fragment key={item.view}>
              {item.group !== menuItems[index - 1]?.group && (
                <p className="nav-group">{item.group}</p>
              )}
              <Nav
                label={item.label}
                icon={item.icon}
                active={item.active}
                onClick={() => {
                  setBusca("");
                  setPecaMovimento("");
                  change(item.view);
                }}
              />
            </Fragment>
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
              // O próximo login pode ser de outra pessoa ou empresa: nada do cache fica.
              queryClient.clear();
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
          {(allowed("PECAS") || allowed("ORDENS")) && (
            <form
              className="global-search"
              role="search"
              onSubmit={(event) => {
                event.preventDefault();
                const termo = buscaGlobal.trim();
                if (!termo) return;
                const destino = destinoDaBusca(termo, allowed);
                if (!destino) return;
                setBusca(destino.busca);
                change(destino.view);
                setBuscaGlobal("");
              }}
            >
              <Search size={16} aria-hidden="true" />
              <input
                type="search"
                value={buscaGlobal}
                onChange={(event) => setBuscaGlobal(event.target.value)}
                placeholder="Código da peça, placa ou nº da OS"
                aria-label="Buscar peça, veículo ou ordem de serviço"
                aria-keyshortcuts="Control+K"
              />
              <kbd aria-hidden="true">Ctrl K</kbd>
            </form>
          )}
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
          <Views
            view={view}
            user={user}
            go={change}
            avisar={setNotice}
            busca={busca}
            pecaMovimento={pecaMovimento}
            setPecaMovimento={setPecaMovimento}
          />
        </section>
      </main>
    </div>
  );
}
