import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Badge, Button, Empty, Loading } from "../../components/ui";

import { label } from "../../lib/format";
import { type Order } from "../../types/domain";
import { mudarStatus } from "./mudarStatus";
import { ATIVAS, encerra } from "./status";

const SO_MINHAS_STORAGE_KEY = "sputnik.oficina-so-minhas";

/** Próximos passos que fazem sentido em cada status ativo. */
const PASSOS: Record<string, [string, string][]> = {
  ABERTA: [["EM_ANDAMENTO", "Iniciar serviço"]],
  EM_ANDAMENTO: [
    ["AGUARDANDO_PECA", "Aguardando peça"],
    ["CONCLUIDA", "Concluir"],
  ],
  AGUARDANDO_PECA: [
    ["EM_ANDAMENTO", "Retomar"],
    ["CONCLUIDA", "Concluir"],
  ],
};

const normalizar = (texto?: string | number | null) =>
  String(texto ?? "")
    .trim()
    .toLocaleLowerCase("pt-BR");

export function Workshop({
  go,
  usuario,
}: {
  go: (v: string) => void;
  usuario: string;
}) {
  const { data: items, error } = useQuery<Order[]>({ queryKey: ["/ordens"] });
  const [erroEnvio, setErro] = useState("");
  const erro = erroEnvio || error?.message || "";
  const [salvando, setSalvando] = useState(0);
  const [soMinhas, setSoMinhas] = useState(
    () => window.localStorage.getItem(SO_MINHAS_STORAGE_KEY) === "true",
  );
  const avancar = async (ordem: Order, status: string) => {
    if (
      encerra(status) &&
      !confirm(
        `Concluir a OS #${ordem.id}? Depois disso ela não aceita novos itens.`,
      )
    )
      return;
    setErro("");
    setSalvando(ordem.id);
    try {
      await mudarStatus(ordem, status);
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setSalvando(0);
    }
  };
  if (!items)
    return erro ? <div className="alert error">{erro}</div> : <Loading />;
  // shortcut: "minhas" compara o consultor técnico da OS com o nome do usuário; trocar por um responsável vinculado ao usuário quando existir.
  const minhas = (o: Order) =>
    normalizar(o.documento?.consultorTecnico) === normalizar(usuario);
  const ativas = items
    .filter((o) => ATIVAS.includes(o.status))
    .filter((o) => !soMinhas || minhas(o));
  return (
    <div className="module-shell space-y-5 workshop">
      <div className="toolbar">
        <div>
          <h2>Serviços na oficina</h2>
          <p>Toque no próximo passo de cada carro. O resto fica na OS.</p>
        </div>
        <label className="workshop-filter">
          <input
            type="checkbox"
            checked={soMinhas}
            onChange={(e) => {
              setSoMinhas(e.target.checked);
              window.localStorage.setItem(
                SO_MINHAS_STORAGE_KEY,
                String(e.target.checked),
              );
            }}
          />
          Só as minhas (consultor: {usuario})
        </label>
      </div>
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      {ativas.length ? (
        <div className="workshop-grid">
          {ativas.map((o) => (
            <article key={o.id} className="workshop-card">
              <div className="workshop-card-top">
                <b className="workshop-plate">{o.veiculo.placa}</b>
                <Badge status={o.status} />
              </div>
              <p>
                <b>OS #{o.id}</b> · {o.veiculo.marcaModelo}
                <small>{o.cliente.nome}</small>
              </p>
              <p className="workshop-desc">{o.descricao}</p>
              <div className="workshop-actions">
                {PASSOS[o.status].map(([status, texto]) => (
                  <Button
                    key={status}
                    size="lg"
                    variant={status === "CONCLUIDA" ? "default" : "secondary"}
                    disabled={salvando === o.id}
                    onClick={() => avancar(o, status)}
                    aria-label={`${texto}: OS #${o.id}`}
                    title={`Mover para ${label(status).toLowerCase()}`}
                  >
                    {texto}
                  </Button>
                ))}
                <Button
                  size="lg"
                  variant="link"
                  onClick={() => go("order-" + o.id)}
                >
                  Abrir OS
                </Button>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <Empty
          text={
            soMinhas
              ? "Nenhuma OS ativa com você como consultor técnico."
              : "Nenhuma OS em aberto agora."
          }
        />
      )}
    </div>
  );
}
