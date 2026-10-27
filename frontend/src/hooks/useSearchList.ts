import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";

/** Lista de catálogo com busca remota, compartilhada por clientes, veículos e peças. */
export function useSearchList<T>(endpoint: string, initialQuery = "") {
  const [query, setQuery] = useState(initialQuery);
  const [termo, setTermo] = useState(initialQuery);
  // Espera a pessoa parar de digitar antes de consultar a API.
  useEffect(() => {
    const timer = window.setTimeout(() => setTermo(query), query ? 250 : 0);
    return () => window.clearTimeout(timer);
  }, [query]);
  const consulta = useQuery<T[]>({
    queryKey: [`${endpoint}?busca=${encodeURIComponent(termo)}`],
    // A tabela atual fica na tela até a nova busca chegar, sem piscar a cada tecla.
    placeholderData: keepPreviousData,
  });
  return {
    items: consulta.error ? [] : (consulta.data ?? []),
    query,
    setQuery,
    error: consulta.error?.message ?? "",
    loading: consulta.isPending,
  };
}
