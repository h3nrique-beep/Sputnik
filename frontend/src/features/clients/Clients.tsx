import { useEffect, useState } from "react";
import { Button, Table } from "../../components/ui";
import { api } from "../../lib/api";
import { type Client } from "../../types/domain";

export function Clients({ go }: { go: (v: string) => void }) {
  const [items, setItems] = useState<Client[]>([]);
  const [query, setQuery] = useState("");
  useEffect(() => {
    api<Client[]>("/clientes?busca=" + encodeURIComponent(query)).then(
      setItems,
    );
  }, [query]);
  return (
    <>
      <div className="toolbar">
        <input
          placeholder="Buscar por nome, CPF ou telefone"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <Button onClick={() => go("clients-new")}>
          ＋ Novo cliente
        </Button>
      </div>
      <Table
        headers={["Nome", "CPF", "Telefone"]}
        rows={items.map((c) => [c.nome, c.cpf, c.telefone])}
      />
    </>
  );
}
