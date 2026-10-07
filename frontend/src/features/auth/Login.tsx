import { type FormEvent, useState } from "react";
import { PackageSearch, ClipboardList, RefreshCw } from "lucide-react";
import { api } from "../../lib/api";
import { type User } from "../../types/domain";

const selfSignupEnabled = import.meta.env.VITE_SELF_SIGNUP_ENABLED === "true";

export function Login({ onLogin }: { onLogin: (u: User) => void }) {
  const [acesso, setAcesso] = useState(""),
    [senha, setSenha] = useState(""),
    [erro, setErro] = useState(""),
    [mostrar, setMostrar] = useState(false),
    [cadastro, setCadastro] = useState(false),
    [empresaNome, setEmpresaNome] = useState(""),
    [slug, setSlug] = useState(""),
    [nomeAdministrador, setNomeAdministrador] = useState(""),
    [entrando, setEntrando] = useState(false);
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (entrando) return;
    setErro("");
    setEntrando(true);
    try {
      onLogin(
        await api<User>(cadastro ? "/assinaturas/cadastro" : "/auth/login", {
          method: "POST",
          body: JSON.stringify(
            cadastro
              ? {
                  empresaNome,
                  slug: slug.toLowerCase(),
                  nomeAdministrador,
                  email: acesso,
                  senha,
                }
              : { acesso, senha },
          ),
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
          <img src="/images/logo.jpg" alt="" width="42" height="42" />
          <span>
            <b>SPUTNIK</b>
            <small>Estoque automotivo e oficina</small>
          </span>
        </div>
        <div className="login-message">
          <h1>
            Seu estoque em dia.
            <br />
            Sua operação no controle.
          </h1>
          <p>Estoque, ordens de serviço e atendimento em um só lugar.</p>
          <ul>
            <li>
              <PackageSearch size={20} aria-hidden="true" /> Peças,
              disponibilidade e localização
            </li>
            <li>
              <ClipboardList size={20} aria-hidden="true" /> Ordens de serviço e
              atendimento
            </li>
            <li>
              <RefreshCw size={20} aria-hidden="true" /> Movimentações,
              histórico e reposição
            </li>
          </ul>
        </div>
        <small className="login-footer">Sistema de gestão operacional</small>
      </section>
      <section className="login-access">
        <form onSubmit={submit}>
          <div className="access-brand">
            <img src="/images/logo.jpg" alt="" width="42" height="42" />
            <span>
              <b>SPUTNIK</b>
              <small>Estoque automotivo e oficina</small>
            </span>
          </div>
          <h2>{cadastro ? "Crie sua conta" : "Acesse sua conta"}</h2>
          <p>
            {cadastro
              ? "Comece seu teste de 14 dias no plano Starter."
              : "Entre com seu e-mail ou usuário para continuar."}
          </p>
          {erro && (
            <div className="alert error" role="alert">
              {erro}
            </div>
          )}
          {cadastro && (
            <>
              <label>
                Nome da oficina
                <input
                  required
                  value={empresaNome}
                  onChange={(e) => setEmpresaNome(e.target.value)}
                />
              </label>
              <label>
                Endereço da empresa
                <input
                  required
                  value={slug}
                  onChange={(e) =>
                    setSlug(
                      e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, ""),
                    )
                  }
                  placeholder="minha-oficina"
                />
              </label>
              <label>
                Seu nome
                <input
                  required
                  value={nomeAdministrador}
                  onChange={(e) => setNomeAdministrador(e.target.value)}
                />
              </label>
            </>
          )}
          <label>
            {cadastro ? "E-mail" : "E-mail ou usuário"}
            <input
              placeholder="Informe seu acesso"
              value={acesso}
              onChange={(e) => setAcesso(e.target.value)}
              required
              autoComplete="username"
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
                autoComplete={cadastro ? "new-password" : "current-password"}
              />
              <button type="button" onClick={() => setMostrar(!mostrar)}>
                {mostrar ? "Ocultar" : "Mostrar"}
              </button>
            </span>
          </label>
          <button className="primary" disabled={entrando}>
            {entrando
              ? "Aguarde..."
              : cadastro
                ? "Criar conta de teste"
                : "Entrar no sistema"}
          </button>
          {!cadastro && (
            <details className="password-help">
              <summary>Esqueci minha senha</summary>
              <p>Peça ao administrador da oficina para redefinir seu acesso.</p>
            </details>
          )}
          {selfSignupEnabled && (
            <button
              type="button"
              className="link mt-3"
              onClick={() => {
                setCadastro(!cadastro);
                setErro("");
              }}
            >
              {cadastro
                ? "Já tenho uma conta"
                : "Criar conta para minha oficina"}
            </button>
          )}
        </form>
      </section>
    </div>
  );
}
