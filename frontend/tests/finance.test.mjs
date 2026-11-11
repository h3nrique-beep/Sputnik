import test from "node:test";
import assert from "node:assert/strict";
import { rentabilidade, somar } from "../src/features/finance/totais.ts";

const lancamentos = [
  { tipo: "RECEITA", status: "PENDENTE", valor: 100 },
  { tipo: "RECEITA", status: "PAGO", valor: 250.5, ordemServicoId: 7 },
  { tipo: "DESPESA", status: "PENDENTE", valor: 40 },
  { tipo: "DESPESA", status: "PAGO", valor: 80, ordemServicoId: 7 },
  { tipo: "RECEITA", status: "PAGO", valor: 30, ordemServicoId: 9 },
  { tipo: "RECEITA", status: "PENDENTE", valor: 999, ordemServicoId: 9 },
];

test("indicadores somam por tipo e status", () => {
  assert.deepEqual(somar(lancamentos), {
    receber: 1099,
    pagar: 40,
    recebido: 280.5,
  });
  assert.deepEqual(somar([]), { receber: 0, pagar: 0, recebido: 0 });
});

test("rentabilidade considera só lançamentos pagos vinculados a uma OS", () => {
  assert.deepEqual(rentabilidade(lancamentos), [
    { orderId: 7, receita: 250.5, despesa: 80 },
    { orderId: 9, receita: 30, despesa: 0 },
  ]);
});
