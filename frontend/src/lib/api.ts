import { QueryClient } from "@tanstack/react-query";

let csrfToken: string | undefined;

/**
 * Cache das leituras: a chave da consulta é a própria URL da API, então
 * `useQuery<Order[]>({ queryKey: ["/ordens"] })` basta em qualquer tela.
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      queryFn: ({ queryKey, signal }) => api(String(queryKey[0]), { signal }),
      staleTime: 30_000,
      retry: 1,
    },
  },
});

/** Rotas após as quais o servidor emite um novo token anti-CSRF para a sessão. */
const ROTAS_QUE_RENOVAM_TOKEN = new Set([
  "/auth/login",
  "/auth/logout",
  "/assinaturas/cadastro",
]);

async function obterTokenCsrf() {
  if (csrfToken) return csrfToken;
  const response = await fetch("/api/security/csrf", {
    credentials: "include",
  });
  if (!response.ok)
    throw new Error("Não foi possível preparar uma solicitação segura.");
  const body = (await response.json()) as { token: string };
  csrfToken = body.token;
  return csrfToken;
}

/** Cliente HTTP único da interface React para a API de sessão do Spring. */
export async function api<T>(url: string, options: RequestInit = {}) {
  const metodo = (options.method || "GET").toUpperCase();
  const requerToken = !["GET", "HEAD", "OPTIONS"].includes(metodo);
  const enviar = async (token?: string) => {
    const headers = new Headers(options.headers);
    if (!headers.has("Content-Type") && !(options.body instanceof FormData))
      headers.set("Content-Type", "application/json");
    if (token) headers.set("X-CSRF-Token", token);
    return fetch(`/api${url}`, {
      ...options,
      headers,
      credentials: "include",
    });
  };

  let response = await enviar(requerToken ? await obterTokenCsrf() : undefined);
  if (requerToken && response.status === 403) {
    // Um token pode expirar junto com a sessão; ele é renovado uma vez antes de falhar.
    csrfToken = undefined;
    response = await enviar(await obterTokenCsrf());
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.mensagem || "Não foi possível concluir a operação.");
  }

  if (ROTAS_QUE_RENOVAM_TOKEN.has(url)) {
    csrfToken = undefined;
  }
  // Toda gravação pode mudar várias telas (estoque, OS, painel): as abertas recarregam.
  if (requerToken) void queryClient.invalidateQueries();

  return response.status === 204
    ? (undefined as T)
    : (response.json() as Promise<T>);
}

/** Faz download de um arquivo protegido pela mesma sessão da aplicação. */
export async function downloadApi(url: string, filename: string) {
  const response = await fetch(`/api${url}`, { credentials: "include" });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.mensagem || "Não foi possível baixar o arquivo.");
  }
  const arquivo = await response.blob();
  const link = document.createElement("a");
  link.href = URL.createObjectURL(arquivo);
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  // Revogar no mesmo ciclo pode cancelar o download em alguns navegadores.
  setTimeout(() => URL.revokeObjectURL(link.href), 0);
}
