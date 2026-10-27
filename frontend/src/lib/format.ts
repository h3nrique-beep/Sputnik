export const money = (value: number) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(
    value || 0,
  );

export const dateTime = (value: string) =>
  new Date(value).toLocaleString("pt-BR");

/** Data sem hora (AAAA-MM-DD) no dia civil certo, sem deslocamento de fuso. */
export const date = (value: string) =>
  new Date(value + "T12:00:00").toLocaleDateString("pt-BR");

/** Nomes exibidos para os códigos de status, perfil e plano vindos da API. */
const ROTULOS: Record<string, string> = {
  ORCAMENTO: "Orçamento",
  ABERTA: "Aberta",
  EM_ANDAMENTO: "Em andamento",
  AGUARDANDO_PECA: "Aguardando peça",
  CONCLUIDA: "Concluída",
  CANCELADA: "Cancelada",
  PENDENTE: "Pendente",
  PAGO: "Pago",
  ATIVA: "Ativa",
  EM_TESTE: "Em teste",
  SUSPENSA: "Suspensa",
  ADMIN: "Administrador",
  BALCONISTA: "Balconista",
  STARTER: "Starter",
  PROFISSIONAL: "Profissional",
  PREMIUM: "Premium",
};

export const label = (code: string) =>
  ROTULOS[code] ?? code.replaceAll("_", " ").toLowerCase();

export const PERMISSOES: [string, string][] = [
  ["PECAS", "Peças e movimentações"],
  ["ORDENS", "Ordens de serviço"],
  ["FINANCEIRO", "Financeiro"],
  ["RELATORIO_MENSAL", "Relatório mensal"],
  ["REPOSICAO", "Reposição"],
];

export const permissionLabel = (code: string) =>
  PERMISSOES.find(([key]) => key === code)?.[1] ?? label(code);

/** Data civil local, sem a conversão UTC que pode antecipar o vencimento. */
export const localDate = (date: Date) =>
  [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, "0"),
    String(date.getDate()).padStart(2, "0"),
  ].join("-");

/** "+12% vs. set"; vazio quando não há base de comparação. */
export const variacao = (atual: number, anterior: number, rotulo: string) => {
  if (!anterior) return atual ? `sem valor em ${rotulo}` : "";
  const pct = Math.round(((atual - anterior) / anterior) * 100);
  return `${pct > 0 ? "+" : ""}${pct}% vs. ${rotulo}`;
};

/** CSV com ";" (padrão do Excel em pt-BR) e aspas escapadas. */
export const paraCsv = (linhas: (string | number)[][]) =>
  linhas
    .map((linha) =>
      linha.map((c) => `"${String(c).replaceAll('"', '""')}"`).join(";"),
    )
    .join("\r\n");
