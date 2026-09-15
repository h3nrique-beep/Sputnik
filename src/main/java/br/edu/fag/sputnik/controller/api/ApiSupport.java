package br.edu.fag.sputnik.controller.api;

import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/** Autorização compartilhada; mantém as respostas e permissões existentes. */
public abstract class ApiSupport {
    protected final AutorizacaoService autorizacao;

    protected ApiSupport(AutorizacaoService autorizacao) {
        this.autorizacao = autorizacao;
    }

    protected boolean temAcesso(HttpSession session, PermissaoModulo permissao) {
        return autorizacao.temAcesso(session, permissao);
    }

    protected ResponseEntity<?> naoAutorizado() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    protected ResponseEntity<?> proibido() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    protected ResponseEntity<Map<String, String>> invalido(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
    }

    protected String operador(HttpSession session) {
        return autorizacao.usuarioAtual(session).orElseThrow().nomeExibicao();
    }
}
