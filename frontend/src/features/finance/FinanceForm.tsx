import { type FormEvent, useState } from "react";
import { Button } from "../../components/ui";
import { api } from "../../lib/api";
import { localDate } from "../../lib/format";
import { type Order } from "../../types/domain";

/** Novo lançamento; `orders` vazio esconde o vínculo com a OS. */
export function FinanceForm({
  orders,
  failed,
}: {
  orders?: Order[];
  failed: (mensagem: string) => void;
}) {
  const vazio = () => ({
    tipo: "RECEITA",
    descricao: "",
    categoria: "",
    valor: "",
    vencimento: localDate(new Date()),
    ordemServicoId: "",
  });
  const [form, setForm] = useState(vazio);
  const [salvando, setSalvando] = useState(false);
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    failed("");
    setSalvando(true);
    try {
      await api("/financeiro", {
        method: "POST",
        body: JSON.stringify({
          ...form,
          valor: Number(form.valor),
          ordemServicoId: form.ordemServicoId
            ? Number(form.ordemServicoId)
            : null,
        }),
      });
      setForm(vazio());
    } catch (e) {
      failed((e as Error).message);
    } finally {
      setSalvando(false);
    }
  };
  return (
    <form className="card compact-form" onSubmit={submit}>
      <h2>Novo lançamento</h2>
      <label>
        Tipo
        <select
          value={form.tipo}
          onChange={(e) => setForm({ ...form, tipo: e.target.value })}
        >
          <option value="RECEITA">Receita</option>
          <option value="DESPESA">Despesa</option>
        </select>
      </label>
      <label>
        Descrição
        <input
          required
          value={form.descricao}
          onChange={(e) => setForm({ ...form, descricao: e.target.value })}
        />
      </label>
      <label>
        Categoria
        <input
          value={form.categoria}
          onChange={(e) => setForm({ ...form, categoria: e.target.value })}
          placeholder="Ex.: mão de obra, aluguel"
        />
      </label>
      <label>
        Valor
        <input
          required
          min="0.01"
          step="0.01"
          type="number"
          value={form.valor}
          onChange={(e) => setForm({ ...form, valor: e.target.value })}
        />
      </label>
      <label>
        Vencimento
        <input
          required
          type="date"
          value={form.vencimento}
          onChange={(e) => setForm({ ...form, vencimento: e.target.value })}
        />
      </label>
      {orders && (
        <label>
          OS vinculada (opcional)
          <select
            value={form.ordemServicoId}
            onChange={(e) =>
              setForm({ ...form, ordemServicoId: e.target.value })
            }
          >
            <option value="">Nenhuma</option>
            {orders.map((order) => (
              <option key={order.id} value={order.id}>
                OS #{order.id} · {order.cliente.nome}
              </option>
            ))}
          </select>
        </label>
      )}
      <Button disabled={salvando}>
        {salvando ? "Adicionando..." : "Adicionar lançamento"}
      </Button>
    </form>
  );
}
