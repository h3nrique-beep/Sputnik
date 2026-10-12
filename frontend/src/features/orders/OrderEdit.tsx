import { type FormEvent, useState } from "react";
import { api } from "../../lib/api";
import { label } from "../../lib/format";
import { type Order } from "../../types/domain";
import { type OrderDocumentData } from "../../types/order-document";
import { OrderDocumentFields } from "./components/OrderDocumentFields";

export function OrderEdit({
  order,
  saved,
  failed,
}: {
  order: Order;
  saved: () => void;
  failed: (m: string) => void;
}) {
  const [documento, setDocumento] = useState<OrderDocumentData>(
    order.documento ?? {},
  );
  const [descricao, setDescricao] = useState(order.descricao),
    [km, setKm] = useState(String(order.quilometragem)),
    [status, setStatus] = useState(order.status),
    [saving, setSaving] = useState(false);
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    const encerrando =
      status !== order.status && ["CONCLUIDA", "CANCELADA"].includes(status);
    if (
      encerrando &&
      !confirm(
        `Marcar a OS #${order.id} como ${label(status).toLowerCase()}? Depois disso ela não aceita novos itens.`,
      )
    )
      return;
    setSaving(true);
    try {
      await api("/ordens/" + order.id, {
        method: "PUT",
        body: JSON.stringify({
          descricao,
          quilometragemAtual: Number(km),
          status,
          documento,
        }),
      });
      saved();
    } catch (e) {
      failed((e as Error).message);
    } finally {
      setSaving(false);
    }
  };
  return (
    <form className="card compact-form" onSubmit={submit}>
      <h2>Editar OS</h2>
      <p className="os-edit-hint">
        Atualize o atendimento e use “Editar dados da OS” para completar os
        campos que aparecem na via impressa.
      </p>
      <label>
        Status
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          {[
            "ABERTA",
            "EM_ANDAMENTO",
            "AGUARDANDO_PECA",
            "CONCLUIDA",
            "CANCELADA",
          ].map((s) => (
            <option key={s} value={s}>
              {label(s)}
            </option>
          ))}
        </select>
      </label>
      <label>
        Quilometragem atual
        <input
          required
          type="number"
          min="0"
          value={km}
          onChange={(e) => setKm(e.target.value)}
        />
      </label>
      <label>
        Descrição
        <textarea
          required
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
        />
      </label>
      <OrderDocumentFields value={documento} onChange={setDocumento} />
      <button className="primary" disabled={saving}>
        {saving ? "Salvando..." : "Salvar alterações"}
      </button>
    </form>
  );
}
