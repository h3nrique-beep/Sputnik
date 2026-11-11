import {
  BarChart3,
  CalendarDays,
  CircleDollarSign,
  ClipboardList,
  Cog,
  LayoutDashboard,
  Package,
  PackageSearch,
  RefreshCw,
  UsersRound,
  Wrench,
} from "lucide-react";
import { type User } from "../types/domain";
import { podeAbrir } from "./navigation";

const GRUPOS_MENU = ["Operação", "Estoque", "Cadastros", "Gestão"];

/** Itens do menu lateral que o usuário pode abrir, já na ordem dos grupos. */
export function itensDoMenu(view: string, user: User) {
  const allowed = (permission: string) =>
    user.perfil === "ADMIN" || user.permissoes.includes(permission);
  const dashboardAllowed = podeAbrir("dashboard", user);
  return (
    [
      dashboardAllowed && {
        label: "Visão geral",
        group: "Operação",
        icon: <LayoutDashboard size={18} />,
        active: view === "dashboard",
        view: "dashboard",
      },
      allowed("PECAS") && {
        label: "Estoque de peças",
        group: "Estoque",
        icon: <Package size={18} />,
        active: view.startsWith("pieces"),
        view: "pieces",
      },
      allowed("ORDENS") && {
        label: "Central de cadastros",
        group: "Cadastros",
        icon: <PackageSearch size={18} />,
        active:
          view === "cadastros" ||
          view.startsWith("clients") ||
          view.startsWith("vehicles"),
        view: "cadastros",
      },
      allowed("PECAS") && {
        label: "Movimentar estoque",
        group: "Estoque",
        icon: <RefreshCw size={18} />,
        active: view === "movement",
        view: "movement",
      },
      allowed("ORDENS") && {
        label: "Ordens de serviço",
        group: "Operação",
        icon: <ClipboardList size={18} />,
        active: view.startsWith("orders") || view.startsWith("order-"),
        view: "orders",
      },
      allowed("ORDENS") && {
        label: "Agenda",
        group: "Operação",
        icon: <CalendarDays size={18} />,
        active: view === "schedule",
        view: "schedule",
      },
      allowed("RELATORIO_MENSAL") && {
        label: "Relatório mensal",
        group: "Gestão",
        icon: <BarChart3 size={18} />,
        active: view === "report",
        view: "report",
      },
      allowed("FINANCEIRO") && {
        label: "Financeiro",
        group: "Gestão",
        icon: <CircleDollarSign size={18} />,
        active: view === "finance",
        view: "finance",
      },
      allowed("REPOSICAO") && {
        label: "Reposição",
        group: "Estoque",
        icon: <Wrench size={18} />,
        active: view === "reposition",
        view: "reposition",
      },
      user.perfil === "ADMIN" && {
        label: "Usuários",
        group: "Gestão",
        icon: <UsersRound size={18} />,
        active: view === "users",
        view: "users",
      },
      user.perfil === "ADMIN" && {
        label: "Desenvolvedor",
        group: "Gestão",
        icon: <Cog size={18} />,
        active: view === "developer",
        view: "developer",
      },
    ]
      .filter((item) => item !== false)
      // Ordem estável por grupo; dentro do grupo mantém a ordem declarada.
      .sort(
        (a, b) => GRUPOS_MENU.indexOf(a.group) - GRUPOS_MENU.indexOf(b.group),
      )
  );
}
