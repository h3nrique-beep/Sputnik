import { type FormEvent, useEffect, useState } from "react";
import { SearchSelect } from "../../components/ui";
import { api } from "../../lib/api";
import { type Piece } from "../../types/domain";

export function StockMovement({
  done,
  initialPieceId = "",
}: {
  done: () => void;
  initialPieceId?: string;
}) {
  const [pieces, setPieces] = useState<Piece[]>([]),
    [pieceId, setPieceId] = useState(initialPieceId),
    [tipo, setTipo] = useState("ENTRADA"),
    [quantidade, setQuantidade] = useState("1"),
    [observacao, setObservacao] = useState(""),
    [erro, setErro] = useState(""),
    [aviso, setAviso] = useState("");
  const [saving, setSaving] = useState(false);
  const peca = pieces.find((p) => String(p.id) === pieceId);
  const qtd = Number(quantidade) || 0;
  const saidaInvalida = tipo === "SAIDA" && !!peca && qtd > peca.disponivel;
  const saldoApos = peca
    ? peca.disponivel + (tipo === "SAIDA" ? -qtd : qtd)
    : null;
  useEffect(() => {
    api<Piece[]>("/pecas")
      .then(setPieces)
      .catch((e) => setErro(e.message));
  }, []);
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (saving) return;
    setSaving(true);
    setErro("");
    setAviso("");
    try {
      await api("/movimentacoes", {
        method: "POST",
        body: JSON.stringify({
          pecaId: Number(pieceId),
          tipo,
          quantidade: Number(quantidade),
          observacao,
        }),
      });
      setPieceId("");
      setQuantidade("1");
      setObservacao("");
      done();
      await api<Piece[]>("/pecas")
        .then(setPieces)
        .catch(() =>
          setAviso(
            "Movimentação registrada. Reabra esta tela para atualizar os saldos.",
          ),
        );
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setSaving(false);
    }
  };
  return (
    <form className="form-card movement-form" onSubmit={submit}>
      <h2>Registrar entrada ou saída</h2>
      <p>O saldo contábil e disponível são atualizados automaticamente.</p>
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      {aviso && (
        <div className="alert success" role="status">
          {aviso}
        </div>
      )}
      <SearchSelect
        label="Peça"
        required
        value={pieceId}
        onChange={setPieceId}
        searchPlaceholder="Código ou nome da peça"
        options={pieces.map((p) => ({
          value: String(p.id),
          label: `${p.codigo} · ${p.nome} · ${p.disponivel} disponível`,
        }))}
      />
      <label>
        Tipo
        <select value={tipo} onChange={(e) => setTipo(e.target.value)}>
          <option value="ENTRADA">Entrada</option>
          <option value="SAIDA">Saída</option>
        </select>
      </label>
      <label>
        Quantidade
        <input
          required
          min="1"
          type="number"
          value={quantidade}
          onChange={(e) => setQuantidade(e.target.value)}
        />
      </label>
      <label>
        Observação
        <textarea
          maxLength={255}
          value={observacao}
          onChange={(e) => setObservacao(e.target.value)}
          placeholder="Motivo da movimentação"
        />
      </label>
      {peca && (
        <p
          className={`form-feedback ${saidaInvalida ? "warning" : ""}`}
          role="status"
        >
          {saidaInvalida
            ? `Saída bloqueada: só há ${peca.disponivel} disponível de ${peca.codigo}.`
            : `Disponível agora: ${peca.disponivel}. Depois desta movimentação: ${saldoApos}.`}
        </p>
      )}
      <button className="primary" disabled={saving || saidaInvalida}>
        {saving ? "Registrando…" : "Registrar movimentação"}
      </button>
    </form>
  );
}
