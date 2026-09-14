import { Button, Table } from "../../components/ui";
import { useSearchList } from "../../hooks/useSearchList";
import { type Client } from "../../types/domain";

export function Clients({ go }: { go: (v: string) => void }) {
  const { items, query, setQuery } = useSearchList<Client>("/clientes");
  return (
    <div className="module-shell space-y-5">
      <div className="toolbar">
        <input
          aria-label="Buscar clientes"
          placeholder="Buscar por nome, CPF ou telefone"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <Button onClick={() => go("clients-new")}>＋ Novo cliente</Button>
      </div>
      <Table
        headers={["Nome", "CPF", "Telefone"]}
        rows={items.map((c) => [c.nome, c.cpf, c.telefone])}
      />
    </div>
  );
}
