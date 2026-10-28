const pathByView: Record<string, string> = {
  dashboard: "/sistemas",
  cadastros: "/sistemas/cadastros",
  clients: "/sistemas/clientes",
  "clients-new": "/sistemas/clientes/novo",
  vehicles: "/sistemas/veiculos",
  "vehicles-new": "/sistemas/veiculos/novo",
  pieces: "/sistemas/estoque",
  "pieces-new": "/sistemas/estoque/nova",
  movement: "/sistemas/estoque/movimentacoes",
  reposition: "/sistemas/estoque/reposicao",
  orders: "/sistemas/ordens",
  "orders-new": "/sistemas/ordens/nova",
  "quotes-new": "/sistemas/orcamentos/novo",
  schedule: "/sistemas/agenda",
  report: "/sistemas/relatorios/mensal",
  finance: "/sistemas/financeiro",
  users: "/sistemas/usuarios",
  developer: "/sistemas/desenvolvedor",
};

const viewByPath = Object.fromEntries(
  Object.entries(pathByView).map(([view, path]) => [path, view]),
) as Record<string, string>;

export const viewFromPath = (pathname: string) => {
  const path = pathname.replace(/\/+$/, "") || "/";
  const order = path.match(/^\/sistemas\/ordens\/(\d+)$/);
  return order ? `order-${order[1]}` : viewByPath[path];
};

export const pathForView = (view: string) => {
  const order = view.match(/^order-(\d+)$/);
  return order
    ? `/sistemas/ordens/${order[1]}`
    : (pathByView[view] ?? "/sistemas");
};

export const title = (v: string) =>
  v === "dashboard"
    ? "Visão geral"
    : v === "cadastros"
      ? "Central de cadastros"
      : v === "movement"
        ? "Movimentar estoque"
        : v === "schedule"
          ? "Agenda"
          : v === "finance"
            ? "Financeiro operacional"
            : v === "report"
              ? "Relatório mensal"
              : v === "reposition"
                ? "Reposição"
                : v === "users"
                  ? "Usuários"
                  : v === "developer"
                    ? "Desenvolvedor"
                    : v.startsWith("pieces")
                      ? v === "pieces-new"
                        ? "Cadastrar peça"
                        : "Estoque de peças"
                      : v.startsWith("clients")
                        ? v === "clients-new"
                          ? "Cadastrar cliente"
                          : "Clientes"
                        : v.startsWith("vehicles")
                          ? v === "vehicles-new"
                            ? "Cadastrar veículo"
                            : "Veículos"
                          : v === "quotes-new"
                            ? "Novo orçamento"
                            : v === "orders-new"
                              ? "Nova ordem de serviço"
                              : v.startsWith("order-")
                                ? "Detalhes da OS"
                                : "Ordens de serviço";

export const subtitle = (v: string) =>
  v === "dashboard"
    ? "Estoque automotivo, reposição e atendimento"
    : v === "cadastros"
      ? "Peças, clientes e veículos para a operação diária"
      : v === "pieces"
        ? "Disponibilidade, preço e localização de cada item"
        : v === "movement"
          ? "Entradas e saídas com rastreabilidade de estoque"
          : v === "reposition"
            ? "Priorize os itens que precisam de compra"
            : v === "quotes-new"
              ? "Monte a proposta; o estoque só muda após a aprovação do cliente"
              : v === "schedule"
                ? "Próximos atendimentos da oficina"
                : v === "finance"
                  ? "Valores previstos e concluídos nas ordens de serviço"
                  : v === "orders-new"
                    ? "Selecione o cliente e o veículo para registrar o atendimento"
                    : v === "developer"
                      ? "Monitoramento, manutenção e cópias seguras do sistema"
                      : "Gestão simples, segura e organizada";

// Placa antiga (ABC-1234) ou Mercosul (ABC1D23).
const PLACA = /^[A-Z]{3}-?\d[A-Z0-9]\d{2}$/i;

/** Decide para onde a busca do topo leva: nº da OS, placa ou peça. */
export const destinoDaBusca = (
  termo: string,
  allowed: (permission: string) => boolean,
): { view: string; busca: string } | null => {
  const texto = termo.trim();
  if (!texto) return null;
  if (/^#?\d+$/.test(texto) && allowed("ORDENS"))
    return { view: `order-${texto.replace("#", "")}`, busca: "" };
  if (PLACA.test(texto) && allowed("ORDENS"))
    return { view: "vehicles", busca: texto.toUpperCase() };
  if (allowed("PECAS")) return { view: "pieces", busca: texto };
  if (allowed("ORDENS")) return { view: "vehicles", busca: texto };
  return null;
};
