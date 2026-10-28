import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";

const css = (name) =>
  readFileSync(new URL(`../src/styles/${name}.css`, import.meta.url), "utf8");

test("views usam a largura disponível, inclusive com formulário compacto", () => {
  assert.match(
    css("theme"),
    /\.content\s*\{[^}]*max-width:\s*none;[^}]*width:\s*100%/,
  );
  assert.match(css("theme"), /\.module-shell\s*\{[^}]*max-width:\s*none/);
  assert.doesNotMatch(
    css("layout-print"),
    /\.content:has\(\.compact-form\)\s*\{\s*max-width:\s*980px/,
  );
});

test("varredura do console roda continuamente e respeita movimento reduzido", () => {
  const motion = css("developer");
  assert.match(motion, /@keyframes developer-rise/);
  assert.match(motion, /developer-scan 7s linear infinite/);
  assert.doesNotMatch(motion, /animation-play-state: paused/);
  assert.match(
    motion,
    /@media \(prefers-reduced-motion: reduce\)\s*\{[^}]*animation:\s*none/s,
  );
});
