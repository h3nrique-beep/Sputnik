import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";

test("modal da OS fica fora do formulário e não força altura de tela inteira", () => {
  const source = readFileSync(
    new URL(
      "../src/features/orders/components/OrderDocumentFields.tsx",
      import.meta.url,
    ),
    "utf8",
  );
  const css = readFileSync(
    new URL("../src/styles/order-document.css", import.meta.url),
    "utf8",
  );
  assert.match(source, /createPortal\([\s\S]*document\.body/);
  assert.doesNotMatch(css, /min-height:\s*100(?:d)?vh/);
  assert.match(
    css,
    /@media print\s*\{\s*\.os-document-overlay\s*\{\s*display:\s*none !important/,
  );
});
