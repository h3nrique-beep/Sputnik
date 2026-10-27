package br.edu.fag.sputnik.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/** Administração segura dos backups do banco H2 utilizado pelo Sputnik. */
@Service
public class BackupService {
    private static final DateTimeFormatter NOME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String PADRAO_NOME = "sputnik-.*\\.zip";

    private final JdbcTemplate jdbc;
    // Lock em vez de synchronized: no Java 21 um monitor preso durante I/O fixa a thread virtual.
    private final ReentrantLock criando = new ReentrantLock();
    private final Path raiz = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    private final Path pastaBackups = raiz.resolve("backups").normalize();
    private final Path banco = raiz.resolve("data").resolve("sputnik.mv.db").normalize();

    public BackupService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public BackupInfo criar() {
        criando.lock();
        try {
            Files.createDirectories(pastaBackups);
            String nome =
                    "sputnik-"
                            + NOME.format(LocalDateTime.now())
                            + "-"
                            + UUID.randomUUID()
                            + ".zip";
            Path destino = pastaBackups.resolve(nome).normalize();
            if (!destino.startsWith(pastaBackups))
                throw new IllegalStateException("Destino de backup inválido.");

            // O comando do H2 produz uma cópia consistente mesmo com a aplicação em funcionamento.
            jdbc.execute("BACKUP TO '" + destino.toString().replace("'", "''") + "'");
            return paraInfo(destino);
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível criar o backup do banco.", exception);
        } finally {
            criando.unlock();
        }
    }

    public List<BackupInfo> listar() {
        try {
            if (!Files.isDirectory(pastaBackups)) return List.of();
            try (var arquivos = Files.list(pastaBackups)) {
                return arquivos.filter(Files::isRegularFile)
                        .filter(arquivo -> arquivo.getFileName().toString().matches(PADRAO_NOME))
                        .sorted(Comparator.comparing(this::ultimaModificacao).reversed())
                        .map(this::paraInfo)
                        .toList();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler os backups.", exception);
        }
    }

    public Path localizar(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.matches(PADRAO_NOME))
            throw new IllegalArgumentException("Backup inválido.");
        Path arquivo = pastaBackups.resolve(nomeArquivo).normalize();
        if (!arquivo.startsWith(pastaBackups) || !Files.isRegularFile(arquivo))
            throw new IllegalArgumentException("Backup não encontrado.");
        return arquivo;
    }

    public Diagnostico diagnostico() {
        try {
            var disco = Files.getFileStore(raiz);
            Runtime runtime = Runtime.getRuntime();
            return new Diagnostico(
                    "2.0.0",
                    System.getProperty("java.version"),
                    System.getProperty("os.name"),
                    bytesEmMb(runtime.totalMemory() - runtime.freeMemory()),
                    bytesEmMb(runtime.maxMemory()),
                    bytesEmMb(disco.getTotalSpace()),
                    bytesEmMb(disco.getUsableSpace()),
                    Files.exists(banco) ? bytesEmMb(Files.size(banco)) : 0,
                    listar().size());
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Não foi possível consultar o ambiente técnico.", exception);
        }
    }

    private BackupInfo paraInfo(Path arquivo) {
        try {
            return new BackupInfo(
                    arquivo.getFileName().toString(),
                    Files.size(arquivo),
                    Files.getLastModifiedTime(arquivo).toMillis());
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler o backup.", exception);
        }
    }

    private FileTime ultimaModificacao(Path arquivo) {
        try {
            return Files.getLastModifiedTime(arquivo);
        } catch (IOException exception) {
            return FileTime.fromMillis(0);
        }
    }

    private long bytesEmMb(long bytes) {
        return Math.max(0, bytes / 1024 / 1024);
    }

    public record BackupInfo(String nome, long tamanhoBytes, long criadoEm) {}

    public record Diagnostico(
            String versao,
            String java,
            String sistemaOperacional,
            long memoriaUsadaMb,
            long memoriaMaximaMb,
            long discoTotalMb,
            long discoLivreMb,
            long bancoMb,
            int backups) {}
}
