import { useEffect, useState } from "react";
import { Copy, KeyRound, ShieldCheck, Trash2, X, Plus } from "lucide-react";
import { Badge, Button, Table } from "../../components/ui";
import { api } from "../../lib/api";
import { permissionLabel } from "../../lib/format";
import { UserCreate } from "./UserCreate";
import { UserPermissions } from "./UserPermissions";

export function Users() {
  const [items, setItems] = useState<any[]>([]),
    [erro, setErro] = useState(""),
    [showCreate, setShowCreate] = useState(false),
    [editing, setEditing] = useState<any>(),
    [senhaTemporaria, setSenhaTemporaria] = useState(""),
    [copiada, setCopiada] = useState(false);
  const load = () =>
    api<any[]>("/admin/usuarios")
      .then(setItems)
      .catch((e) => setErro(e.message));
  useEffect(() => {
    load();
  }, []);
  const reset = async (id: number, nome: string) => {
    if (
      !confirm(
        `Gerar uma nova senha temporária para ${nome}? A senha atual deixa de funcionar.`,
      )
    )
      return;
    try {
      const resposta = await api<{ senhaTemporaria: string }>(
        "/admin/usuarios/" + id + "/resetar-senha",
        { method: "POST" },
      );
      setSenhaTemporaria(resposta.senhaTemporaria);
      load();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  const remove = async (id: number, nome: string) => {
    if (
      !confirm(`Remover o acesso de ${nome}? Esta ação não pode ser desfeita.`)
    )
      return;
    try {
      await api("/admin/usuarios/" + id, { method: "DELETE" });
      load();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  return (
    <>
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      {senhaTemporaria && (
        <div className="alert success temp-password" role="status">
          <p>
            Senha temporária gerada: <code>{senhaTemporaria}</code>. Entregue ao
            usuário por um canal seguro. Ela não aparece de novo depois que você
            fechar este aviso.
          </p>
          <span className="row-actions">
            <Button
              variant="outline"
              size="sm"
              onClick={() =>
                navigator.clipboard
                  ?.writeText(senhaTemporaria)
                  .then(() => setCopiada(true))
                  .catch(() => setCopiada(false))
              }
            >
              <Copy size={15} aria-hidden="true" />
              {copiada ? "Copiada" : "Copiar senha"}
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                setSenhaTemporaria("");
                setCopiada(false);
              }}
            >
              <X size={15} aria-hidden="true" /> Fechar
            </Button>
          </span>
        </div>
      )}
      <div className="toolbar">
        <div>
          <h2>Acessos do sistema</h2>
          <p>Cadastre balcões e defina os módulos disponíveis.</p>
        </div>
        <Button onClick={() => setShowCreate(!showCreate)}>
          <Plus size={16} aria-hidden="true" /> Novo usuário
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
          key={editing.id}
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
            : u.permissoes.map(permissionLabel).join(", ") || "Sem módulos",
          u.perfil === "ADMIN" ? (
            <span className="muted">Protegido</span>
          ) : (
            <span className="row-actions">
              <Button variant="outline" size="sm" onClick={() => setEditing(u)}>
                <ShieldCheck size={15} aria-hidden="true" /> Permissões
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="row-action-icon"
                onClick={() => reset(u.id, u.nome)}
                aria-label={`Resetar senha de ${u.nome}`}
                title="Resetar senha"
              >
                <KeyRound size={15} aria-hidden="true" />
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="row-action-icon row-action-danger"
                onClick={() => remove(u.id, u.nome)}
                aria-label={`Remover ${u.nome}`}
                title="Remover usuário"
              >
                <Trash2 size={15} aria-hidden="true" />
              </Button>
            </span>
          ),
        ])}
      />
    </>
  );
}
