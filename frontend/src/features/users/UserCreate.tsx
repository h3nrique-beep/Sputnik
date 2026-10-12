import { type FormEvent, useState } from "react";
import { api } from "../../lib/api";
import { PermissionFields } from "./PermissionFields";

export function UserCreate({ done }: { done: () => void }) {
  const [form, setForm] = useState<any>({ permissoes: [] }),
    [erro, setErro] = useState("");
  const toggle = (value: string) =>
    setForm({
      ...form,
      permissoes: form.permissoes.includes(value)
        ? form.permissoes.filter((p: string) => p !== value)
        : [...form.permissoes, value],
    });
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await api("/admin/usuarios", {
        method: "POST",
        body: JSON.stringify(form),
      });
      done();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  return (
    <form className="form-card" onSubmit={submit}>
      <h2>Novo balconista</h2>
      <p>
        O usuário usará o e-mail no login e deverá definir uma nova senha no
        primeiro acesso.
      </p>
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      <label>
        Nome completo
        <input
          required
          value={form.nome || ""}
          onChange={(e) => setForm({ ...form, nome: e.target.value })}
        />
      </label>
      <label>
        E-mail
        <input
          required
          type="email"
          value={form.email || ""}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
        />
      </label>
      <label>
        Senha temporária
        <input
          required
          type="text"
          value={form.senha || ""}
          onChange={(e) => setForm({ ...form, senha: e.target.value })}
        />
      </label>
      <PermissionFields selected={form.permissoes} toggle={toggle} />
      <button className="primary">Cadastrar usuário</button>
    </form>
  );
}
