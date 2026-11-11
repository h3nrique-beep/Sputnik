import { useState } from "react";
import {
  Badge,
  Button,
  Card,
  Empty,
  Table,
  confirmar,
} from "../../components/ui";
import { api, baixarArquivo } from "../../lib/api";
import { date, localDate, money, paraCsv } from "../../lib/format";
import { type FinanceEntry as Entry } from "../../types/domain";

/** Lançamentos do período: baixa individual, baixa em lote e exportação. */
export function FinanceEntries({
  entries,
  failed,
}: {
  entries: Entry[];
  failed: (mensagem: string) => void;
}) {
  const [selecionados, setSelecionados] = useState<Set<number>>(new Set());
  const [baixandoLote, setBaixandoLote] = useState(false);
  const [baixandoId, setBaixandoId] = useState<number>();
  const baixar = async (id: number) => {
    failed("");
    setBaixandoId(id);
    try {
      await api("/financeiro/" + id + "/baixar", { method: "POST" });
    } catch (e) {
      failed((e as Error).message);
    } finally {
      setBaixandoId(undefined);
    }
  };
  const pendentes = entries.filter((e) => e.status === "PENDENTE");
  const marcados = pendentes.filter((e) => selecionados.has(e.id));
  const somaMarcados = marcados.reduce((sum, e) => sum + Number(e.valor), 0);
  const alternar = (id: number) =>
    setSelecionados((atual) => {
      const proximo = new Set(atual);
      if (!proximo.delete(id)) proximo.add(id);
      return proximo;
    });
  const baixarSelecionados = async () => {
    const varios = marcados.length > 1;
    if (
      !(await confirmar(
        `Baixar ${marcados.length} ${varios ? "lançamentos" : "lançamento"}?`,
        `${money(somaMarcados)} ${varios ? "serão marcados" : "será marcado"} como pago com a data de hoje.`,
        { acao: "Baixar" },
      ))
    )
      return;
    failed("");
    setBaixandoLote(true);
    const falhas: string[] = [];
    // Um por vez: cada baixa é independente e uma falha não desfaz as outras.
    for (const entry of marcados) {
      try {
        await api("/financeiro/" + entry.id + "/baixar", { method: "POST" });
      } catch (e) {
        falhas.push(`${entry.descricao}: ${(e as Error).message}`);
      }
    }
    setBaixandoLote(false);
    setSelecionados(new Set());
    if (falhas.length) failed("Não baixados: " + falhas.join(" · "));
  };
  const exportar = () => {
    const linhas = (marcados.length ? marcados : entries).map((e) => [
      e.tipo === "RECEITA" ? "Receita" : "Despesa",
      e.descricao,
      e.categoria ?? "",
      e.vencimento,
      Number(e.valor).toFixed(2).replace(".", ","),
      e.status === "PAGO" ? "Pago" : "Pendente",
      e.ordemServicoId ? "#" + e.ordemServicoId : "",
    ]);
    const csv = paraCsv([
      ["Tipo", "Descrição", "Categoria", "Vencimento", "Valor", "Status", "OS"],
      ...linhas,
    ]);
    // BOM para o Excel reconhecer UTF-8 e manter os acentos.
    baixarArquivo(
      new Blob(["﻿" + csv], { type: "text/csv;charset=utf-8" }),
      `lancamentos-${localDate(new Date())}.csv`,
    );
  };
  return (
    <Card title="Lançamentos">
      {entries.length > 0 && (
        <div className="bulk-bar" role="group" aria-label="Ações em lote">
          <span aria-live="polite">
            {marcados.length
              ? `${marcados.length} selecionado(s) · ${money(somaMarcados)}`
              : "Marque os pendentes para baixar de uma vez."}
          </span>
          {pendentes.length > 0 && (
            <Button
              size="sm"
              variant="secondary"
              onClick={() =>
                setSelecionados(
                  marcados.length === pendentes.length
                    ? new Set()
                    : new Set(pendentes.map((e) => e.id)),
                )
              }
            >
              {marcados.length === pendentes.length
                ? "Limpar seleção"
                : "Selecionar pendentes"}
            </Button>
          )}
          <Button size="sm" variant="secondary" onClick={exportar}>
            {marcados.length ? "Exportar selecionados" : "Exportar CSV"}
          </Button>
          <Button
            size="sm"
            onClick={baixarSelecionados}
            disabled={!marcados.length || baixandoLote}
          >
            {baixandoLote ? "Baixando..." : "Baixar selecionados"}
          </Button>
        </div>
      )}
      {entries.length ? (
        <Table
          headers={[
            "Seleção",
            "Tipo",
            "Descrição",
            "Vencimento",
            "Valor",
            "Status",
            "Ação",
          ]}
          rows={entries.map((entry) => [
            entry.status === "PENDENTE" ? (
              <input
                key="sel"
                type="checkbox"
                className="row-check"
                checked={selecionados.has(entry.id)}
                onChange={() => alternar(entry.id)}
                aria-label={`Selecionar ${entry.descricao}`}
              />
            ) : (
              ""
            ),
            entry.tipo === "RECEITA" ? "Receita" : "Despesa",
            <span key={entry.id}>
              {entry.descricao}
              <small className="block text-(--muted)">
                {entry.categoria || "Sem categoria"}
                {entry.ordemServicoId ? " · OS #" + entry.ordemServicoId : ""}
              </small>
            </span>,
            date(entry.vencimento),
            money(entry.valor),
            <Badge key="status" status={entry.status} />,
            entry.status === "PENDENTE" ? (
              <Button
                key="baixar"
                size="sm"
                onClick={() => baixar(entry.id)}
                disabled={baixandoLote || baixandoId !== undefined}
              >
                {baixandoId === entry.id ? "Baixando..." : "Baixar"}
              </Button>
            ) : (
              "Pago"
            ),
          ])}
        />
      ) : (
        <Empty text="Nenhum lançamento para o período selecionado." />
      )}
    </Card>
  );
}
