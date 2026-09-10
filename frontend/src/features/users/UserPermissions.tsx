import { type FormEvent, useState } from "react";
import { api } from "../../lib/api";
import { PermissionFields } from "./PermissionFields";

export function UserPermissions({
  user,
  done,
}: {
  user: any;
  done: () => void;
}) {
  const [permissoes, setPermissoes] = useState<string[]>(user.permissoes),
    [erro, setErro] = useState("");
  const toggle = (value: string) =>
    setPermissoes(
      permissoes.includes(value)
        ? permissoes.filter((p) => p !== value)
        : [...permissoes, value],
    );
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await api("/admin/usuarios/" + user.id + "/permissoes", {
        method: "PUT",
        body: JSON.stringify({ permissoes }),
      });
      done();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  return (
    <form className="form-card" onSubmit={submit}>
      <h2>Permissões de {user.nome}</h2>
      {erro && <div className="alert error">{erro}</div>}
      <PermissionFields selected={permissoes} toggle={toggle} />
      <button className="primary">Salvar permissões</button>
    </form>
  );
}
