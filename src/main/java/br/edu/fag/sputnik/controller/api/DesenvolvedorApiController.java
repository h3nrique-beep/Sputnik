package br.edu.fag.sputnik.controller.api;

import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.BackupService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DesenvolvedorApiController extends ApiSupport {
    private final BackupService backups;

    public DesenvolvedorApiController(AutorizacaoService autorizacao, BackupService backups) {
        super(autorizacao);
        this.backups = backups;
    }

    @GetMapping("/admin/desenvolvedor")
    public ResponseEntity<?> diagnosticoDesenvolvedor(HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        return ResponseEntity.ok(backups.diagnostico());
    }

    @GetMapping("/admin/desenvolvedor/backups")
    public ResponseEntity<?> listarBackups(HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        return ResponseEntity.ok(backups.listar());
    }

    @PostMapping("/admin/desenvolvedor/backups")
    public ResponseEntity<?> criarBackup(HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(backups.criar());
        } catch (IllegalStateException exception) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("mensagem", "Não foi possível criar o backup agora."));
        }
    }

    @GetMapping("/admin/desenvolvedor/backups/{nome:.+}")
    public ResponseEntity<?> baixarBackup(@PathVariable String nome, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        try {
            var arquivo = backups.localizar(nome);
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + arquivo.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(arquivo))
                    .body(Files.readAllBytes(arquivo));
        } catch (Exception exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
