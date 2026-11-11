import test from "node:test";
import assert from "node:assert/strict";
import { destinoDaBusca, subtitle, title } from "../src/app/navigation.ts";

const tudo = () => true;
const soEstoque = (p) => p === "PECAS";

test("busca do topo: número abre a OS, placa abre veículos, resto busca peça", () => {
  assert.deepEqual(destinoDaBusca("148", tudo), { view: "order-148", busca: "" });
  assert.deepEqual(destinoDaBusca("#12", tudo), { view: "order-12", busca: "" });
  assert.deepEqual(destinoDaBusca("bra2e19", tudo), { view: "vehicles", busca: "BRA2E19" });
  assert.deepEqual(destinoDaBusca("ABC-1234", tudo), { view: "vehicles", busca: "ABC-1234" });
  assert.deepEqual(destinoDaBusca("FO-102", tudo), { view: "pieces", busca: "FO-102" });
});

test("busca respeita permissões e ignora texto vazio", () => {
  assert.deepEqual(destinoDaBusca("148", soEstoque), { view: "pieces", busca: "148" });
  assert.equal(destinoDaBusca("FO-102", () => false), null);
  assert.equal(destinoDaBusca("   ", tudo), null);
});

test("toda rota tem título e subtítulo próprios", () => {
  assert.equal(title("order-7"), "Detalhes da OS");
  assert.equal(title("clients-new"), "Cadastrar cliente");
  assert.notEqual(subtitle("clients"), subtitle("orders"));
});
