import { type FormEvent, useEffect, useState } from "react";
import { api } from "../../lib/api";
import { type Piece } from "../../types/domain";

export function StockMovement({ done }: { done: () => void }) {
  const [pieces, setPieces] = useState<Piece[]>([]),
    [pieceId, setPieceId] = useState(""),
    [tipo, setTipo] = useState("ENTRADA"),
    [quantidade, setQuantidade] = useState("1"),
    [observacao, setObservacao] = useState(""),
    [erro, setErro] = useState("");
  const [saving, setSaving] = useState(false);
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
          setErro(
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
    <form className="form-card" onSubmit={submit}>
      <h2>Registrar entrada ou saída</h2>
      <p>O saldo contábil e disponível são atualizados automaticamente.</p>
      {erro && <div className="alert error">{erro}</div>}
      <label>
        Peça
        <select
          required
          value={pieceId}
          onChange={(e) => setPieceId(e.target.value)}
        >
          <option value="">Selecione</option>
          {pieces.map((p) => (
            <option key={p.id} value={p.id}>
              {p.codigo} · {p.nome} ({p.disponivel} disponível)
            </option>
          ))}
        </select>
      </label>
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
      <button className="primary" disabled={saving}>
        {saving ? "Registrando…" : "Registrar movimentação"}
      </button>
    </form>
  );
}
