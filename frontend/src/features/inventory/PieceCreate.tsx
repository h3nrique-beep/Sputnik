import { type FormEvent, useState } from "react";
import { api } from "../../lib/api";

export function PieceCreate({ done }: { done: () => void }) {
  const [form, setForm] = useState<any>({
      saldo: 0,
      estoqueDisponivel: 0,
      estoqueMinimo: 0,
    }),
    [erro, setErro] = useState("");
  const [saving, setSaving] = useState(false);
  const set = (key: string, value: any) => setForm({ ...form, [key]: value });
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (saving) return;
    setSaving(true);
    setErro("");
    try {
      await api("/pecas", {
        method: "POST",
        body: JSON.stringify({
          ...form,
          preco: Number(form.preco),
          saldo: Number(form.saldo),
          estoqueDisponivel: Number(form.estoqueDisponivel),
          estoqueMinimo: Number(form.estoqueMinimo),
        }),
      });
      done();
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setSaving(false);
    }
  };
  return (
    <form className="form-card grid-form" onSubmit={submit}>
      <h2>Novo item de estoque</h2>
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      <label>
        Código
        <input
          required
          value={form.codigo || ""}
          onChange={(e) => set("codigo", e.target.value)}
        />
      </label>
      <label>
        Nome da peça
        <input
          required
          value={form.nome || ""}
          onChange={(e) => set("nome", e.target.value)}
        />
      </label>
      <label>
        Fabricante
        <input
          required
          value={form.fabricante || ""}
          onChange={(e) => set("fabricante", e.target.value)}
        />
      </label>
      <label>
        Categoria
        <input
          required
          value={form.categoria || ""}
          onChange={(e) => set("categoria", e.target.value)}
        />
      </label>
      <label>
        Fornecedor
        <input
          required
          value={form.fornecedor || ""}
          onChange={(e) => set("fornecedor", e.target.value)}
        />
      </label>
      <label>
        Locação
        <input
          required
          placeholder="Corredor C · Prateleira 2"
          value={form.localizacao || ""}
          onChange={(e) => set("localizacao", e.target.value)}
        />
      </label>
      <label>
        Preço de venda
        <input
          required
          type="number"
          min="0.01"
          step="0.01"
          value={form.preco || ""}
          onChange={(e) => set("preco", e.target.value)}
        />
      </label>
      <label>
        Estoque contábil
        <input
          required
          type="number"
          min="0"
          value={form.saldo}
          onChange={(e) => set("saldo", e.target.value)}
        />
      </label>
      <label>
        Estoque disponível
        <input
          required
          type="number"
          min="0"
          value={form.estoqueDisponivel}
          onChange={(e) => set("estoqueDisponivel", e.target.value)}
        />
      </label>
      <label>
        Estoque mínimo
        <input
          required
          type="number"
          min="0"
          value={form.estoqueMinimo}
          onChange={(e) => set("estoqueMinimo", e.target.value)}
        />
      </label>
      <button className="primary" disabled={saving}>
        {saving ? "Salvando…" : "Cadastrar peça"}
      </button>
    </form>
  );
}
