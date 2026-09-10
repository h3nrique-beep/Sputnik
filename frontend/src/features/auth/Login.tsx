import { type FormEvent, useState } from "react";
import { api } from "../../lib/api";
import { type User } from "../../types/domain";

export function Login({ onLogin }: { onLogin: (u: User) => void }) {
  const [acesso, setAcesso] = useState(""),
    [senha, setSenha] = useState(""),
    [erro, setErro] = useState(""),
    [mostrar, setMostrar] = useState(false),
    [entrando, setEntrando] = useState(false);
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (entrando) return;
    setErro("");
    setEntrando(true);
    try {
      onLogin(
        await api<User>("/auth/login", {
          method: "POST",
          body: JSON.stringify({ acesso, senha }),
        }),
      );
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setEntrando(false);
    }
  };
  return (
    <div className="login">
      <section className="login-presentation">
        <div className="login-brand">
          <img src="/images/logo.jpg" />
          <span>
            <b>SPUTNIK</b>
            <small>Gestão de Oficina</small>
          </span>
        </div>
        <div className="login-message">
          <h1>
            Gestão de oficina,
            <br />
            sem perder o controle.
          </h1>
          <p>Estoque, ordens de serviço e atendimento em um só lugar.</p>
          <ul>
            <li>Consulta de peças e estoque em segundos</li>
            <li>Atendimentos organizados por status</li>
            <li>Histórico e reposição sempre atualizados</li>
          </ul>
        </div>
        <small className="login-footer">Sistema de gestão operacional</small>
      </section>
      <section className="login-access">
        <form onSubmit={submit}>
          <div className="access-brand">
            <img src="/images/logo.jpg" />
            <span>
              <b>SPUTNIK</b>
              <small>Gestão de Oficina</small>
            </span>
          </div>
          <h2>Acesse sua conta</h2>
          <p>Entre com seu e-mail ou usuário para continuar.</p>
          {erro && <div className="alert error">{erro}</div>}
          <label>
            E-mail ou usuário
            <input
              placeholder="Informe seu acesso"
              value={acesso}
              onChange={(e) => setAcesso(e.target.value)}
              required
              autoFocus
            />
          </label>
          <label>
            Senha
            <span className="password-field">
              <input
                type={mostrar ? "text" : "password"}
                placeholder="Informe sua senha"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                required
              />
              <button type="button" onClick={() => setMostrar(!mostrar)}>
                {mostrar ? "Ocultar" : "Mostrar"}
              </button>
            </span>
          </label>
          <button className="primary" disabled={entrando}>
            {entrando ? "Entrando..." : "Entrar no sistema"}
          </button>
        </form>
      </section>
    </div>
  );
}
