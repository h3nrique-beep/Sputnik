import { useEffect, useRef, useState } from "react";
import {
  CircleCheck,
  Clock3,
  Pause,
  Play,
  Plus,
  ShieldCheck,
} from "lucide-react";
import { Loading, Table } from "../../components/ui";
import { api, downloadApi } from "../../lib/api";
import {
  type BackupEntry,
  type DeveloperDiagnostics,
} from "../../types/domain";

export function DeveloperConsole() {
  const [diagnostico, setDiagnostico] = useState<DeveloperDiagnostics>();
  const [backups, setBackups] = useState<BackupEntry[]>([]);
  const [erro, setErro] = useState("");
  const [criando, setCriando] = useState(false);
  const heroRef = useRef<HTMLElement>(null);
  const [motionPaused, setMotionPaused] = useState(false);

  useEffect(() => {
    const hero = heroRef.current;
    if (!hero) return;
    let visible = false;
    const update = () =>
      hero.classList.toggle(
        "motion-running",
        visible && !document.hidden && !motionPaused,
      );
    const observer = new IntersectionObserver(([entry]) => {
      visible = entry.isIntersecting;
      update();
    });
    observer.observe(hero);
    document.addEventListener("visibilitychange", update);
    return () => {
      observer.disconnect();
      document.removeEventListener("visibilitychange", update);
      hero.classList.remove("motion-running");
    };
  }, [diagnostico, motionPaused]);

  const carregar = () => {
    Promise.all([
      api<DeveloperDiagnostics>("/admin/desenvolvedor"),
      api<BackupEntry[]>("/admin/desenvolvedor/backups"),
    ])
      .then(([dados, lista]) => {
        setDiagnostico(dados);
        setBackups(lista);
      })
      .catch((erro: Error) => setErro(erro.message));
  };

  useEffect(() => {
    carregar();
  }, []);

  const criarBackup = async () => {
    if (criando) return;
    setCriando(true);
    setErro("");
    try {
      await api<BackupEntry>("/admin/desenvolvedor/backups", {
        method: "POST",
      });
      carregar();
    } catch (erro) {
      setErro((erro as Error).message);
    } finally {
      setCriando(false);
    }
  };

  const baixarBackup = async (backup: BackupEntry) => {
    try {
      await downloadApi(
        `/admin/desenvolvedor/backups/${encodeURIComponent(backup.nome)}`,
        backup.nome,
      );
    } catch (erro) {
      setErro((erro as Error).message);
    }
  };

  if (!diagnostico && !erro) return <Loading />;
  if (!diagnostico) return <div className="alert error">{erro}</div>;

  const memoria = percentual(
    diagnostico.memoriaUsadaMb,
    diagnostico.memoriaMaximaMb,
  );
  const disco = percentual(
    diagnostico.discoTotalMb - diagnostico.discoLivreMb,
    diagnostico.discoTotalMb,
  );
  const memoriaLivre = diagnostico.memoriaMaximaMb - diagnostico.memoriaUsadaMb;
  const discoUsado = diagnostico.discoTotalMb - diagnostico.discoLivreMb;

  return (
    <div className="developer-console">
      <section className="developer-hero" ref={heroRef}>
        <div>
          <h2>Console do Desenvolvedor</h2>
          <p>
            Monitoramento operacional, manutenção e ferramentas seguras para o
            Sputnik.
          </p>
          <span className="developer-tags">
            <span>
              <ShieldCheck size={14} aria-hidden="true" />
              Controle administrativo
            </span>
            <span>
              <Clock3 size={14} aria-hidden="true" />
              Ambiente local
            </span>
            <button
              type="button"
              className="developer-motion-toggle"
              aria-pressed={motionPaused}
              onClick={() => setMotionPaused(!motionPaused)}
            >
              {motionPaused ? (
                <Play size={14} aria-hidden="true" />
              ) : (
                <Pause size={14} aria-hidden="true" />
              )}
              {motionPaused ? "Retomar animação" : "Pausar animação"}
            </button>
          </span>
        </div>
        <div className="developer-runtime">
          <small>Versão do sistema</small>
          <b>v{diagnostico.versao}</b>
          <small>JAVA</small>
          <strong>{diagnostico.java}</strong>
          <small>Ambiente</small>
          <strong>{diagnostico.sistemaOperacional}</strong>
        </div>
      </section>
      {erro && <div className="alert error">{erro}</div>}
      <section className="developer-metrics">
        <Metric
          label="Backups"
          value={diagnostico.backups}
          detail="Arquivos disponíveis"
        />
        <Metric
          label="Banco H2"
          value={formatGb(diagnostico.bancoMb)}
          detail="Base local do Sputnik"
        />
        <Metric
          label="Memória JVM"
          value={`${memoria}%`}
          detail={`${formatGb(diagnostico.memoriaUsadaMb)} usados de ${formatGb(diagnostico.memoriaMaximaMb)}`}
          emphasis={`${formatGb(memoriaLivre)} disponíveis`}
        />
        <Metric
          label="Disco local"
          value={`${disco}%`}
          detail={`${formatGb(discoUsado)} usados de ${formatGb(diagnostico.discoTotalMb)}`}
          emphasis={`${formatGb(diagnostico.discoLivreMb)} livres`}
        />
      </section>
      <section className="developer-health">
        <div className="developer-health-title">
          <div>
            <h2>Saúde do ambiente</h2>
            <p>Resumo técnico do Sputnik neste computador.</p>
          </div>
          <span className="developer-status">
            <CircleCheck size={14} aria-hidden="true" />
            Operacional
          </span>
        </div>
        <div className="developer-summary">
          <Summary
            label="Memória"
            text={`${formatGb(diagnostico.memoriaUsadaMb)} usados · ${formatGb(memoriaLivre)} disponíveis`}
          />
          <Summary
            label="Disco"
            text={`${formatGb(discoUsado)} usados · ${formatGb(diagnostico.discoLivreMb)} livres`}
          />
          <Summary label="Banco" text={formatGb(diagnostico.bancoMb)} />
          <Summary
            label="Backups"
            text={`${diagnostico.backups} disponível(is)`}
          />
        </div>
      </section>
      <section className="developer-backups">
        <div>
          <h2>Backups do banco</h2>
          <p>
            Crie uma cópia consistente do H2 enquanto o Sputnik está em
            funcionamento.
          </p>
        </div>
        <button className="primary" onClick={criarBackup} disabled={criando}>
          <Plus size={16} aria-hidden="true" />
          {criando ? "Criando backup..." : "Criar backup"}
        </button>
      </section>
      <Table
        headers={["Arquivo", "Criado em", "Tamanho", "Ação"]}
        rows={backups.map((backup) => [
          backup.nome,
          new Date(backup.criadoEm).toLocaleString("pt-BR"),
          formatBytes(backup.tamanhoBytes),
          <button
            className="link"
            key={backup.nome}
            onClick={() => baixarBackup(backup)}
          >
            Baixar
          </button>,
        ])}
      />
    </div>
  );
}

function Metric({
  label,
  value,
  detail,
  emphasis,
}: {
  label: string;
  value: string | number;
  detail: string;
  emphasis?: string;
}) {
  return (
    <article>
      <small>{label}</small>
      <b>{value}</b>
      <span>{detail}</span>
      {emphasis && <em>{emphasis}</em>}
    </article>
  );
}

function Summary({ label, text }: { label: string; text: string }) {
  return (
    <span>
      <b>{label}</b>
      {text}
    </span>
  );
}

function percentual(valor: number, total: number) {
  return Math.round((valor / Math.max(1, total)) * 100);
}

function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

function formatGb(valorMb: number) {
  return `${(Math.max(0, valorMb) / 1024).toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} GB`;
}
