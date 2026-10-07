import { useState } from "react";
import { PackagePlus, TriangleAlert, CircleCheck } from "lucide-react";
import { Button, Loading, Table } from "../../components/ui";
import { useSearchList } from "../../hooks/useSearchList";
import { money } from "../../lib/format";
import { type Piece } from "../../types/domain";

export function Pieces({ go }: { go: (v: string) => void }) {
  const [onlyCritical, setOnlyCritical] = useState(false);
  const { items, query, setQuery, error, loading } =
    useSearchList<Piece>("/pecas");
  const critical = items.filter((piece) => piece.disponivel < piece.minimo);
  const visible = onlyCritical ? critical : items;
  return (
    <div className="module-shell space-y-5">
      <div className="toolbar">
        <input
          aria-label="Buscar peças"
          placeholder="Buscar por nome ou código"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <Button onClick={() => go("pieces-new")}>
          <PackagePlus size={17} aria-hidden="true" /> Nova peça
        </Button>
      </div>
      {!loading && !error && (
        <div className="stock-summary">
          <span>
            <strong>{items.length}</strong> itens nesta consulta
          </span>
          <span>
            <strong>{critical.length}</strong> abaixo do mínimo
          </span>
          <Button
            variant="outline"
            size="sm"
            aria-pressed={onlyCritical}
            onClick={() => setOnlyCritical(!onlyCritical)}
          >
            <TriangleAlert size={16} aria-hidden="true" />
            {onlyCritical ? "Mostrar todos" : "Só estoque crítico"}
          </Button>
        </div>
      )}
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
            headers={[
              "Código",
              "Peça",
              "Categoria",
              "Disponível",
              "Mínimo",
              "Situação",
              "Preço",
              "Locação",
            ]}
            emptyText={
              onlyCritical
                ? "Nenhum item abaixo do mínimo nesta consulta."
                : "Nenhuma peça encontrada. Cadastre uma peça ou ajuste a busca."
            }
            rows={visible.map((p) => [
              p.codigo,
              p.nome,
              p.categoria,
              String(p.disponivel),
              String(p.minimo),
              <span
                className={`stock-status ${p.disponivel === 0 ? "zero" : p.disponivel < p.minimo ? "critical" : ""}`}
              >
                {p.disponivel < p.minimo || p.disponivel === 0 ? (
                  <TriangleAlert size={14} aria-hidden="true" />
                ) : (
                  <CircleCheck size={14} aria-hidden="true" />
                )}
                {p.disponivel === 0
                  ? "Sem estoque"
                  : p.disponivel < p.minimo
                    ? "Repor"
                    : "Disponível"}
              </span>,
              money(p.preco),
              p.locacao,
            ])}
          />
        )
      )}
    </div>
  );
}
