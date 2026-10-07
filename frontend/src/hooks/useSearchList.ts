import { useEffect, useState } from "react";
import { api } from "../lib/api";

/** Lista de catálogo com busca remota, compartilhada por clientes, veículos e peças. */
export function useSearchList<T>(endpoint: string) {
  const [items, setItems] = useState<T[]>([]);
  const [query, setQuery] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError("");
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
      });
    return () => controller.abort();
  }, [endpoint, query]);

  return { items, query, setQuery, error, loading };
}
