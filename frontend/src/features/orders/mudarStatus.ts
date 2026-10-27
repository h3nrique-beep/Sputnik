import { api } from "../../lib/api";
import { type Order } from "../../types/domain";

/** Troca só o status, reenviando os demais campos como estão. */
export const mudarStatus = (ordem: Order, status: string) =>
  api(`/ordens/${ordem.id}`, {
    method: "PUT",
    body: JSON.stringify({
      descricao: ordem.descricao,
      quilometragemAtual: ordem.quilometragem,
      status,
      documento: ordem.documento ?? null,
    }),
  });
