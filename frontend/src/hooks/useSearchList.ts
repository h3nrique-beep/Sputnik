import { useEffect, useState } from "react";
import { api } from "../lib/api";

/** Lista de catálogo com busca remota, compartilhada por clientes, veículos e peças. */
export function useSearchList<T>(endpoint: string, initialQuery = "") {
  const [items, setItems] = useState<T[]>([]);
  const [query, setQuery] = useState(initialQuery);
  const [error, setError] = useState("");
  // `loading` cobre só a primeira carga: nas buscas seguintes a tabela atual
  // continua na tela até a resposta chegar, sem piscar a cada tecla.
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const controller = new AbortController();
    setError("");
    // Espera a pessoa parar de digitar antes de consultar a API.
    const timer = window.setTimeout(
      () =>
        api<T[]>(`${endpoint}?busca=${encodeURIComponent(query)}`, {
          signal: controller.signal,
        })
          .then((result) => {
            if (!controller.signal.aborted) setItems(result);
          })
          .catch((e: Error) => {
            if (!controller.signal.aborted) {
              setItems([]);
              setError(e.message);
            }
          })
          .finally(() => {
            if (!controller.signal.aborted) setLoading(false);
          }),
      query ? 250 : 0,
    );
    return () => {
      window.clearTimeout(timer);
      controller.abort();
    };
  }, [endpoint, query]);

  return { items, query, setQuery, error, loading };
}
