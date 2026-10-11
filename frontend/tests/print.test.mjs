import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";

test("impressão esconde apenas o cabeçalho do sistema, preservando a logo da OS", () => {
  const css = readFileSync(
    new URL("../src/styles/layout-print.css", import.meta.url),
    "utf8",
  );
  assert.doesNotMatch(css, /\.shell\s+header\s*,/);
  assert.match(css, /\.shell\s*>\s*main\s*>\s*header\s*,/);
  assert.match(css, /\.sputnik-os-head\s*\{[^}]*display:\s*flex/s);
});
