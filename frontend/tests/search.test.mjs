import test from "node:test";
import assert from "node:assert/strict";
import { filtrarOpcoes } from "../src/lib/search.ts";

const pecas = [
  { value: "1", label: "FO-102 · Filtro de Óleo · 5 disponível" },
  { value: "2", label: "FA-090 · Filtro de Ar · 3 disponível" },
  { value: "3", label: "PF-220 · Pastilha de Freio · 12 disponível" },
];

test("busca ignora acentos e maiúsculas e encontra por código ou nome", () => {
  assert.deepEqual(filtrarOpcoes(pecas, "oleo").map((p) => p.value), ["1"]);
  assert.deepEqual(filtrarOpcoes(pecas, "pf-2").map((p) => p.value), ["3"]);
  assert.deepEqual(filtrarOpcoes(pecas, "filtro").map((p) => p.value), ["1", "2"]);
});

test("busca vazia mostra tudo e a opção escolhida não some", () => {
  assert.equal(filtrarOpcoes(pecas, "   ").length, 3);
  assert.deepEqual(filtrarOpcoes(pecas, "pastilha", "1").map((p) => p.value), ["1", "3"]);
});
