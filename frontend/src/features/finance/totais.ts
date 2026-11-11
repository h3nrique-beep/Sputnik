// "import type" some por inteiro: o teste roda este arquivo direto no Node.
import type { FinanceEntry as Entry } from "../../types/domain";

/** Indicadores do topo: pendentes a receber e a pagar, e receitas já pagas. */
export function somar(lista: Entry[]) {
  const total = (tipo: Entry["tipo"], status: Entry["status"]) =>
    lista
      .filter((entry) => entry.tipo === tipo && entry.status === status)
      .reduce((sum, entry) => sum + Number(entry.valor), 0);
  return {
    receber: total("RECEITA", "PENDENTE"),
    pagar: total("DESPESA", "PENDENTE"),
    recebido: total("RECEITA", "PAGO"),
  };
}

/** Receita e despesa já pagas, agrupadas pela OS vinculada. */
export function rentabilidade(lista: Entry[]) {
  return Object.values(
    lista
      .filter((entry) => entry.ordemServicoId && entry.status === "PAGO")
      .reduce<
        Record<string, { orderId: number; receita: number; despesa: number }>
      >((acc, entry) => {
        const orderId = Number(entry.ordemServicoId);
        const current = acc[orderId] ?? { orderId, receita: 0, despesa: 0 };
        current[entry.tipo === "RECEITA" ? "receita" : "despesa"] += Number(
          entry.valor,
        );
        acc[orderId] = current;
        return acc;
      }, {}),
  );
}
