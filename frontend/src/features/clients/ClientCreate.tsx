import { type FormEvent, useState } from "react";
import { api } from "../../lib/api";

export function ClientCreate({ done }: { done: () => void }) {
  const [form, setForm] = useState<any>({}),
    [erro, setErro] = useState("");
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await api("/clientes", { method: "POST", body: JSON.stringify(form) });
      done();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  return (
    <form className="form-card" onSubmit={submit}>
      <h2>Dados do cliente</h2>
      {erro && <div className="alert error">{erro}</div>}
      <label>
        Nome completo
        <input
          required
          value={form.nome || ""}
          onChange={(e) => setForm({ ...form, nome: e.target.value })}
        />
      </label>
      <label>
        CPF
        <input
          required
          value={form.cpf || ""}
          onChange={(e) => setForm({ ...form, cpf: e.target.value })}
        />
      </label>
      <label>
        Telefone
        <input
          required
          value={form.telefone || ""}
          onChange={(e) => setForm({ ...form, telefone: e.target.value })}
        />
      </label>
      <button className="primary">Cadastrar cliente</button>
    </form>
  );
}
