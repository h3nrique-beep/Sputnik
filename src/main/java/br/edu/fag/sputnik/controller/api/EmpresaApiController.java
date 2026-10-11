package br.edu.fag.sputnik.controller.api;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.domain.Perfil;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.EmpresaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaApiController extends ApiSupport {
    private final EmpresaService empresas;

    public EmpresaApiController(AutorizacaoService autorizacao, EmpresaService empresas) {
        super(autorizacao);
        this.empresas = empresas;
    }

    @GetMapping("/assinatura")
    public ResponseEntity<?> assinatura(HttpSession session) {
        var usuario = SessaoAutenticacao.usuario(session).orElse(null);
        if (usuario == null || usuario.perfil() != Perfil.ADMIN || usuario.empresaId() == null)
            return proibido();
        try {
            return ResponseEntity.ok(empresas.assinatura(usuario.empresaId()));
        } catch (IllegalArgumentException exception) {
            return invalido(exception);
        }
    }
}
