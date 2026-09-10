import { useEffect, useState } from "react";
import { Button, Table } from "../../components/ui";
import { api } from "../../lib/api";
import { money } from "../../lib/format";
import { type Piece } from "../../types/domain";

export function Pieces({ go }: { go: (v: string) => void }) {
  const [items, setItems] = useState<Piece[]>([]);
  const [query, setQuery] = useState("");
  useEffect(() => {
    api<Piece[]>("/pecas?busca=" + encodeURIComponent(query)).then(setItems);
  }, [query]);
  return (
    <>
      <div className="toolbar">
        <input
          placeholder="Buscar por nome ou código"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <Button onClick={() => go("pieces-new")}>
          ＋ Nova peça
        </Button>
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
    </>
  );
}
