import { useEffect, useState } from "react";
import { Badge, Button, Table } from "../../components/ui";
import { api } from "../../lib/api";
import { UserCreate } from "./UserCreate";
import { UserPermissions } from "./UserPermissions";

export function Users() {
  const [items, setItems] = useState<any[]>([]),
    [erro, setErro] = useState(""),
    [showCreate, setShowCreate] = useState(false),
    [editing, setEditing] = useState<any>();
  const load = () =>
    api<any[]>("/admin/usuarios")
      .then(setItems)
      .catch((e) => setErro(e.message));
  useEffect(() => {
    load();
  }, []);
  const reset = async (id: number) => {
    if (!confirm("Redefinir a senha temporária para 12345678?")) return;
    try {
      await api("/admin/usuarios/" + id + "/resetar-senha", { method: "POST" });
      load();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  const remove = async (id: number) => {
    if (!confirm("Remover este usuário?")) return;
    try {
      await api("/admin/usuarios/" + id, { method: "DELETE" });
      load();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  return (
    <>
      {erro && <div className="alert error">{erro}</div>}
      <div className="toolbar">
        <div>
          <h2>Acessos do sistema</h2>
          <p>Cadastre balcões e defina os módulos disponíveis.</p>
        </div>
        <Button onClick={() => setShowCreate(!showCreate)}>
          ＋ Novo usuário
        </Button>
      </div>
      {showCreate && (
        <UserCreate
          done={() => {
            setShowCreate(false);
            load();
          }}
        />
      )}
      {editing && (
        <UserPermissions
          user={editing}
          done={() => {
            setEditing(undefined);
            load();
          }}
        />
      )}
      <Table
        headers={["Nome", "Login", "Perfil", "Módulos", "Ações"]}
        rows={items.map((u) => [
          u.nome,
          u.login,
          <Badge key="role" status={u.perfil} />,
          u.perfil === "ADMIN"
            ? "Acesso total"
            : u.permissoes.join(", ") || "Sem módulos",
          u.perfil === "ADMIN" ? (
            <span className="muted">Protegido</span>
          ) : (
            <span className="row-actions">
              <button className="link" onClick={() => setEditing(u)}>
                Permissões
              </button>
              <button className="link" onClick={() => reset(u.id)}>
                Resetar senha
              </button>
              <button className="link danger" onClick={() => remove(u.id)}>
                Remover
              </button>
            </span>
          ),
        ])}
      />
    </>
  );
}
