/** Colunas do quadro, na ordem do fluxo da oficina. Canceladas ficam fora. */
export const COLUNAS_QUADRO = [
  "ORCAMENTO",
  "ABERTA",
  "EM_ANDAMENTO",
  "AGUARDANDO_PECA",
  "CONCLUIDA",
];

export const ATIVAS = ["ABERTA", "EM_ANDAMENTO", "AGUARDANDO_PECA"];

/**
 * Orçamento só sai do quadro pela aprovação (baixa estoque) e nada volta a ser
 * orçamento; as demais trocas seguem a mesma regra do servidor.
 */
export const podeMover = (de: string, para: string) =>
  de !== para && de !== "ORCAMENTO" && para !== "ORCAMENTO";

export const encerra = (status: string) =>
  ["CONCLUIDA", "CANCELADA"].includes(status);
