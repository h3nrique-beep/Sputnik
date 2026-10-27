import test from "node:test";
import assert from "node:assert/strict";
import { paraCsv, variacao } from "../src/lib/format.ts";
import { podeMover } from "../src/features/orders/status.ts";

test("variação contra o mês anterior", () => {
  assert.equal(variacao(112, 100, "set"), "+12% vs. set");
  assert.equal(variacao(50, 100, "set"), "-50% vs. set");
  assert.equal(variacao(100, 100, "set"), "0% vs. set");
  assert.equal(variacao(10, 0, "set"), "sem valor em set");
  assert.equal(variacao(0, 0, "set"), "");
});

test("CSV escapa aspas e usa ponto e vírgula", () => {
  assert.equal(
    paraCsv([
      ["a", 1],
      ['diz "oi"', "x;y"],
    ]),
    '"a";"1"\r\n"diz ""oi""";"x;y"',
  );
});

test("quadro: orçamento só muda pela aprovação", () => {
  assert.equal(podeMover("ORCAMENTO", "ABERTA"), false);
  assert.equal(podeMover("ABERTA", "ORCAMENTO"), false);
  assert.equal(podeMover("ABERTA", "ABERTA"), false);
  assert.equal(podeMover("ABERTA", "CONCLUIDA"), true);
});
