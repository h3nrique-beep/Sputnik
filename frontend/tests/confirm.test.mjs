import test from "node:test";
import assert from "node:assert/strict";
import { readdirSync, readFileSync } from "node:fs";

const fontes = (dir) =>
  readdirSync(new URL(dir, import.meta.url), { recursive: true })
    .filter((f) => /\.tsx?$/.test(f))
    .map((f) => [f, readFileSync(new URL(dir + f, import.meta.url), "utf8")]);

test("telas usam o diálogo do sistema, não confirm/alert do navegador", () => {
  for (const [arquivo, codigo] of fontes("../src/features/"))
    assert.doesNotMatch(
      codigo,
      /(?<![.\w])(confirm|alert|prompt)\(/,
      `${arquivo} usa diálogo nativo; use confirmar()`,
    );
});
