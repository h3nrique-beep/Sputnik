package br.edu.fag.sputnik.controller.api;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioService;
import br.edu.fag.sputnik.controller.api.mapper.ApiMapper;
import br.edu.fag.sputnik.security.CsrfTokenService;
import br.edu.fag.sputnik.web.CadastroEmpresaForm;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/assinaturas")
public class AssinaturasApiController {
    private final UsuarioService usuarios;
    private final CsrfTokenService csrfTokens;

    @Value("${sputnik.saas.self-signup.enabled:false}")
    private boolean cadastroPublicoHabilitado;

    public AssinaturasApiController(UsuarioService usuarios, CsrfTokenService csrfTokens) {
        this.usuarios = usuarios;
        this.csrfTokens = csrfTokens;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody CadastroEmpresaForm form,
            HttpSession session,
            HttpServletRequest request) {
        if (!cadastroPublicoHabilitado) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(
                            Map.of(
                                    "mensagem",
                                    "Cadastro público será liberado após a migração multiempresa"
                                            + " dos dados operacionais."));
        }
        try {
            var usuario = usuarios.cadastrarEmpresa(form);
            // Mesmo cuidado do login: novo identificador de sessão e novo token anti-CSRF.
            request.changeSessionId();
            SessaoAutenticacao.iniciar(session, usuario);
            csrfTokens.renovar(session);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.usuarioDto(usuario));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }
}
