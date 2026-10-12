import { Plus } from "lucide-react";
import { Button, Loading, Table } from "../../components/ui";
import { useSearchList } from "../../hooks/useSearchList";
import { type Client } from "../../types/domain";

export function Clients({ go }: { go: (v: string) => void }) {
  const { items, query, setQuery, error, loading } =
    useSearchList<Client>("/clientes");
  return (
    <div className="module-shell space-y-5">
      <div className="toolbar">
        <input
          aria-label="Buscar clientes"
          placeholder="Buscar por nome, CPF ou telefone"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <Button onClick={() => go("clients-new")}>
          <Plus size={16} aria-hidden="true" /> Novo cliente
        </Button>
      </div>
      {error && (
        <div className="alert error" role="alert">
          {error}
        </div>
      )}
      {loading ? (
        <Loading />
      ) : (
        !error && (
          <Table
            headers={["Nome", "CPF", "Telefone"]}
            rows={items.map((c) => [c.nome, c.cpf, c.telefone])}
          />
        )
      )}
    </div>
  );
}
