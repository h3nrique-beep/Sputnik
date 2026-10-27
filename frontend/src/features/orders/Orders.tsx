import { useQuery } from "@tanstack/react-query";
import { Columns3, List, Plus } from "lucide-react";
import { type DragEvent, useState } from "react";
import { Badge, Button, Loading, Table, confirmar } from "../../components/ui";
import { queryClient } from "../../lib/api";
import { label } from "../../lib/format";
import { type Order } from "../../types/domain";
import { mudarStatus } from "./mudarStatus";
import { COLUNAS_QUADRO, encerra, podeMover } from "./status";

const MODO_STORAGE_KEY = "sputnik.ordens-modo";

export function Orders({ go }: { go: (v: string) => void }) {
  const { data: items, error } = useQuery<Order[]>({ queryKey: ["/ordens"] });
  const [erroEnvio, setErro] = useState("");
  const erro = erroEnvio || error?.message || "";
  const [quadro, setQuadro] = useState(
    () => window.localStorage.getItem(MODO_STORAGE_KEY) !== "lista",
  );
  const [arrastando, setArrastando] = useState<Order | null>(null);
  const [alvo, setAlvo] = useState("");
  const alternarModo = (proximo: boolean) => {
    setQuadro(proximo);
    window.localStorage.setItem(MODO_STORAGE_KEY, proximo ? "quadro" : "lista");
  };
  const soltar = async (event: DragEvent, status: string) => {
    event.preventDefault();
    setAlvo("");
    const ordem = arrastando;
    setArrastando(null);
    if (!ordem || !podeMover(ordem.status, status)) return;
    if (
      encerra(status) &&
      !(await confirmar(
        `Marcar a OS #${ordem.id} como ${label(status).toLowerCase()}?`,
        "Depois disso ela não aceita novos itens.",
        { acao: `Marcar como ${label(status).toLowerCase()}` },
      ))
    )
      return;
    setErro("");
    // Atualiza na hora e desfaz se o servidor recusar.
    queryClient.setQueryData<Order[]>(["/ordens"], (lista) =>
      lista?.map((o) => (o.id === ordem.id ? { ...o, status } : o)),
    );
    try {
      await mudarStatus(ordem, status);
    } catch (e) {
      queryClient.setQueryData(["/ordens"], items);
      setErro((e as Error).message);
    }
  };
  const canceladas = items?.filter((o) => o.status === "CANCELADA").length ?? 0;
  return (
    <div className="module-shell space-y-5">
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      <div className="toolbar">
        <div>
          <h2>Atendimentos</h2>
          <p>
            {quadro
              ? "Arraste o cartão para mudar o status. Orçamentos mudam pela aprovação."
              : "Controle de serviços, custos e status."}
          </p>
        </div>
        <span className="toolbar-actions">
          <span className="view-toggle" role="group" aria-label="Exibição">
            <button
              type="button"
              aria-pressed={quadro}
              onClick={() => alternarModo(true)}
            >
              <Columns3 size={16} aria-hidden="true" /> Quadro
            </button>
            <button
              type="button"
              aria-pressed={!quadro}
              onClick={() => alternarModo(false)}
            >
              <List size={16} aria-hidden="true" /> Lista
            </button>
          </span>
          <Button variant="secondary" onClick={() => go("quotes-new")}>
            <Plus size={16} aria-hidden="true" /> Novo orçamento
          </Button>
          <Button
            onClick={() => go("orders-new")}
            aria-keyshortcuts="N"
            title="Atalho: N"
          >
            <Plus size={16} aria-hidden="true" /> Nova ordem
          </Button>
        </span>
      </div>
      {!items ? (
        !erro && <Loading />
      ) : quadro ? (
        <>
          <div className="kanban">
            {COLUNAS_QUADRO.map((status) => {
              const cartoes = items.filter((o) => o.status === status);
              const aceita =
                !!arrastando && podeMover(arrastando.status, status);
              return (
                <section
                  key={status}
                  className={`kanban-col${aceita ? " aceita" : ""}${alvo === status && aceita ? " sobre" : ""}`}
                  aria-label={`${label(status)}: ${cartoes.length}`}
                  onDragOver={(event) => {
                    if (!aceita) return;
                    event.preventDefault();
                    setAlvo(status);
                  }}
                  onDragLeave={() => setAlvo("")}
                  onDrop={(event) => soltar(event, status)}
                >
                  <h3>
                    <Badge status={status} />
                    <span>{cartoes.length}</span>
                  </h3>
                  {cartoes.map((o) => (
                    <button
                      type="button"
                      key={o.id}
                      className="kanban-card"
                      draggable={o.status !== "ORCAMENTO"}
                      onDragStart={(event) => {
                        // Firefox só inicia o arrasto com algum dado definido.
                        event.dataTransfer.setData("text/plain", String(o.id));
                        setArrastando(o);
                      }}
                      onDragEnd={() => {
                        setArrastando(null);
                        setAlvo("");
                      }}
                      onClick={() => go("order-" + o.id)}
                    >
                      <b>
                        {o.status === "ORCAMENTO" ? "Orçamento" : "OS"} #{o.id}
                      </b>
                      <span>{o.cliente.nome}</span>
                      <small>
                        {o.veiculo.marcaModelo} · {o.veiculo.placa}
                      </small>
                    </button>
                  ))}
                </section>
              );
            })}
          </div>
          {canceladas > 0 && (
            <p className="muted">
              {canceladas} {canceladas === 1 ? "OS cancelada" : "OS canceladas"}{" "}
              fora do quadro; veja na lista.
            </p>
          )}
        </>
      ) : (
        <Table
          headers={[
            "Atendimento",
            "Cliente",
            "Veículo",
            "KM",
            "Status",
            "Ação",
          ]}
          rows={items.map((o) => [
            `${o.status === "ORCAMENTO" ? "Orçamento" : "OS"} #${o.id}`,
            o.cliente.nome,
            `${o.veiculo.marcaModelo} · ${o.veiculo.placa}`,
            String(o.quilometragem),
            <Badge key="b" status={o.status} />,
            <Button
              key="a"
              size="sm"
              variant="link"
              onClick={() => go("order-" + o.id)}
            >
              {o.status === "ORCAMENTO" ? "Ver orçamento" : "Ver OS"}
            </Button>,
          ])}
        />
      )}
    </div>
  );
}
