import test from "node:test";
import assert from "node:assert/strict";
import { localDate } from "../src/lib/format.ts";

let sequence = 0;
const client = () => import("../src/lib/api.ts?test=" + sequence++);

test("headers personalizados preservam JSON, sessão e CSRF", async (t) => {
  const { api } = await client();
  const calls = [];
  t.mock.method(globalThis, "fetch", async (url, options) => {
    calls.push({ url, options });
    return url.endsWith("/csrf")
      ? Response.json({ token: "token-teste" })
      : new Response(null, { status: 204 });
  });
  await api("/clientes", {
    method: "POST",
    headers: new Headers({ "X-Teste": "ok" }),
    body: "{}",
  });
  assert.equal(calls.length, 2);
  const options = calls[1].options;
  assert.equal(options.headers.get("X-Teste"), "ok");
  assert.equal(options.headers.get("X-CSRF-Token"), "token-teste");
  assert.equal(options.headers.get("Content-Type"), "application/json");
  assert.equal(options.credentials, "include");
});

test("token expirado é renovado uma vez sem repetir indefinidamente", async (t) => {
  const { api } = await client();
  let tokens = 0;
  let posts = 0;
  t.mock.method(globalThis, "fetch", async (url, options) => {
    if (url.endsWith("/csrf"))
      return Response.json({ token: "token-" + ++tokens });
    posts++;
    if (posts === 1)
      return Response.json({ mensagem: "Expirado" }, { status: 403 });
    assert.equal(options.headers.get("X-CSRF-Token"), "token-2");
    return new Response(null, { status: 204 });
  });
  assert.equal(
    await api("/clientes", { method: "POST", body: "{}" }),
    undefined,
  );
  assert.equal(posts, 2);
  assert.equal(tokens, 2);
});

test("GET propaga cancelamento e mensagem de erro sem solicitar token", async (t) => {
  const { api } = await client();
  const controller = new AbortController();
  t.mock.method(globalThis, "fetch", async (url, options) => {
    assert.equal(url, "/api/clientes?busca=teste");
    assert.equal(options.signal, controller.signal);
    return Response.json(
      { mensagem: "Sem acesso ao catálogo" },
      { status: 403 },
    );
  });
  await assert.rejects(
    api("/clientes?busca=teste", { signal: controller.signal }),
    /Sem acesso ao catálogo/,
  );
});

test("FormData não recebe Content-Type JSON", async (t) => {
  const { api } = await client();
  t.mock.method(globalThis, "fetch", async (url, options) => {
    if (url.endsWith("/csrf")) return Response.json({ token: "token" });
    assert.equal(options.headers.has("Content-Type"), false);
    return new Response(null, { status: 204 });
  });
  await api("/upload", { method: "POST", body: new FormData() });
});

test("data financeira mantém o dia local perto da meia-noite", () => {
  assert.equal(localDate(new Date(2026, 8, 24, 23, 59)), "2026-09-24");
  assert.equal(localDate(new Date(2026, 11, 31, 23, 59)), "2026-12-31");
});
