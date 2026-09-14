import { useEffect, useState } from "react";
import { api } from "../lib/api";

/** Lista de catálogo com busca remota, compartilhada por clientes, veículos e peças. */
export function useSearchList<T>(endpoint: string) {
  const [items, setItems] = useState<T[]>([]);
  const [query, setQuery] = useState("");

  useEffect(() => {
    api<T[]>(`${endpoint}?busca=${encodeURIComponent(query)}`).then(setItems);
  }, [endpoint, query]);

  return { items, query, setQuery };
}
