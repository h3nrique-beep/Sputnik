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

// Título (h1 da casca) e subtítulo de cada tela; a view não repete o título.
const TELAS: Record<string, [titulo: string, subtitulo: string]> = {
  dashboard: ["Visão geral", "Estoque automotivo, reposição e atendimento"],
  cadastros: [
    "Central de cadastros",
    "Peças, clientes e veículos para a operação diária",
  ],
  clients: ["Clientes", "Contatos e documentos de quem traz o veículo"],
  "clients-new": [
    "Cadastrar cliente",
    "Dados de contato para abrir atendimentos",
  ],
  vehicles: ["Veículos", "Placa, chassi e quilometragem de cada veículo"],
  "vehicles-new": [
    "Cadastrar veículo",
    "Vincule o veículo ao cliente responsável",
  ],
  pieces: [
    "Estoque de peças",
    "Disponibilidade, preço e localização de cada item",
  ],
  "pieces-new": ["Cadastrar peça", "Código, preço, estoque mínimo e locação"],
  movement: [
    "Movimentar estoque",
    "Entradas e saídas com rastreabilidade de estoque",
  ],
  reposition: ["Reposição", "Priorize os itens que precisam de compra"],
  orders: [
    "Ordens de serviço",
    "Serviços, custos e status de cada atendimento",
  ],
  "orders-new": [
    "Nova ordem de serviço",
    "Selecione o cliente e o veículo para registrar o atendimento",
  ],
  "quotes-new": [
    "Novo orçamento",
    "Monte a proposta; o estoque só muda após a aprovação do cliente",
  ],
  order: ["Detalhes da OS", "Itens, custos, status e documento do atendimento"],
  schedule: ["Agenda", "Próximos atendimentos da oficina"],
  report: ["Relatório mensal", "Entradas e saídas de estoque do mês"],
  finance: [
    "Financeiro operacional",
    "Valores previstos e concluídos nas ordens de serviço",
  ],
  users: ["Usuários", "Quem acessa o sistema e quais módulos enxerga"],
  developer: [
    "Desenvolvedor",
    "Monitoramento, manutenção e cópias seguras do sistema",
  ],
};

const tela = (v: string) =>
  TELAS[v.startsWith("order-") ? "order" : v] ?? TELAS.orders;

export const title = (v: string) => tela(v)[0];

export const subtitle = (v: string) => tela(v)[1];

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
