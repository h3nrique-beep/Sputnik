package br.edu.fag.sputnik.controller.api;

import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


/** Autorização compartilhada; mantém as respostas e permissões existentes. */
public abstract class ApiSupport {
    protected final AutorizacaoService autorizacao;

    protected ApiSupport(AutorizacaoService autorizacao) {
        this.autorizacao = autorizacao;
    }

    protected boolean temAcesso(HttpSession session, PermissaoModulo permissao) {
        return autorizacao.temAcesso(session, permissao);
    }

    protected <T> ResponseEntity<T> naoAutorizado() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    protected <T> ResponseEntity<T> proibido() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    protected String operador(HttpSession session) {
        return autorizacao.usuarioAtual(session).orElseThrow().nomeExibicao();
    }
}
