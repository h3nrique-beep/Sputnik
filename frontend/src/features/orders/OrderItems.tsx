import { type FormEvent, useEffect, useState } from "react";
import { SearchSelect } from "../../components/ui";
import { api } from "../../lib/api";
import { type Piece } from "../../types/domain";

export function OrderItems({
  orderId,
  saved,
  failed,
}: {
  orderId: number;
  saved: () => void;
  failed: (m: string) => void;
}) {
  const [pieces, setPieces] = useState<Piece[]>([]),
    [pieceId, setPieceId] = useState(""),
    [quantidade, setQuantidade] = useState("1"),
    [descricao, setDescricao] = useState(""),
    [valor, setValor] = useState(""),
    [enviando, setEnviando] = useState(false),
    [ok, setOk] = useState("");
  const peca = pieces.find((p) => String(p.id) === pieceId);
  const acimaDoDisponivel = peca && Number(quantidade) > peca.disponivel;
  useEffect(() => {
    api<Piece[]>("/pecas")
      .then(setPieces)
      .catch((e) => failed(e.message));
  }, []);
  const addPiece = async (e: FormEvent) => {
    e.preventDefault();
    if (enviando) return;
    setEnviando(true);
    setOk("");
    try {
      await api("/ordens/" + orderId + "/itens/peca", {
        method: "POST",
        body: JSON.stringify({
          pecaId: Number(pieceId),
          quantidade: Number(quantidade),
        }),
      });
      setOk(`${quantidade}× ${peca?.nome ?? "peça"} adicionada.`);
      setPieceId("");
      setQuantidade("1");
      saved();
    } catch (e) {
      failed((e as Error).message);
    } finally {
      setEnviando(false);
    }
  };
  const addService = async (e: FormEvent) => {
    e.preventDefault();
    if (enviando) return;
    setEnviando(true);
    setOk("");
    try {
      await api("/ordens/" + orderId + "/itens/servico", {
        method: "POST",
        body: JSON.stringify({ descricao, valor: Number(valor) }),
      });
      setOk(`Serviço “${descricao.trim()}” adicionado.`);
      setDescricao("");
      setValor("");
      saved();
    } catch (e) {
      failed((e as Error).message);
    } finally {
      setEnviando(false);
    }
  };
  return (
    <div className="card compact-form">
      <h2>Lançar itens</h2>
      {ok && (
        <p className="form-feedback success" role="status">
          {ok}
        </p>
      )}
      <form onSubmit={addPiece}>
        <SearchSelect
          label="Peça"
          required
          value={pieceId}
          onChange={setPieceId}
          placeholder="Selecione uma peça"
          searchPlaceholder="Código ou nome da peça"
          options={pieces.map((p) => ({
            value: String(p.id),
            label: `${p.codigo} · ${p.nome} · ${p.disponivel} disponível`,
          }))}
        />
        <label>
          Quantidade
          <input
            required
            type="number"
            min="1"
            value={quantidade}
            onChange={(e) => setQuantidade(e.target.value)}
          />
        </label>
        {acimaDoDisponivel && (
          <p className="form-feedback warning" role="status">
            Só há {peca.disponivel} disponível. Em orçamento o item entra; numa
            OS aberta a baixa é recusada.
          </p>
        )}
        <button className="secondary" disabled={enviando}>
          Adicionar peça
        </button>
      </form>
      <hr />
      <form onSubmit={addService}>
        <label>
          Serviço executado
          <input
            required
            value={descricao}
            onChange={(e) => setDescricao(e.target.value)}
          />
        </label>
        <label>
          Valor da mão de obra
          <input
            required
            type="number"
            min="0.01"
            step="0.01"
            value={valor}
            onChange={(e) => setValor(e.target.value)}
          />
        </label>
        <button className="secondary" disabled={enviando}>
          Adicionar serviço
        </button>
      </form>
    </div>
  );
}
