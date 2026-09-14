import { Button, Table } from "../../components/ui";
import { useSearchList } from "../../hooks/useSearchList";
import { money } from "../../lib/format";
import { type Piece } from "../../types/domain";

export function Pieces({ go }: { go: (v: string) => void }) {
  const { items, query, setQuery } = useSearchList<Piece>("/pecas");
  return (
    <div className="module-shell space-y-5">
      <div className="toolbar">
        <input
          aria-label="Buscar peças"
          placeholder="Buscar por nome ou código"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <Button onClick={() => go("pieces-new")}>＋ Nova peça</Button>
      </div>
      <Table
        headers={[
          "Código",
          "Peça",
          "Categoria",
          "Disponível",
          "Preço",
          "Locação",
        ]}
        rows={items.map((p) => [
          p.codigo,
          p.nome,
          p.categoria,
          String(p.disponivel),
          money(p.preco),
          p.locacao,
        ])}
      />
    </div>
  );
}
