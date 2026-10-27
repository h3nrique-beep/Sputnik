package br.edu.fag.sputnik.controller.api;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import br.edu.fag.sputnik.controller.api.dto.UsuarioDto;
import static br.edu.fag.sputnik.controller.api.mapper.ApiMapper.usuarioDto;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioService;
import br.edu.fag.sputnik.controller.api.dto.LoginRequest;
import br.edu.fag.sputnik.security.CsrfTokenService;
import br.edu.fag.sputnik.security.LoginAttemptService;
import br.edu.fag.sputnik.service.AutorizacaoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthApiController extends ApiSupport {
    private final UsuarioService usuarios;
    private final LoginAttemptService tentativasLogin;
    private final CsrfTokenService csrfTokens;

    public AuthApiController(
            AutorizacaoService autorizacao,
            UsuarioService usuarios,
            LoginAttemptService tentativasLogin,
            CsrfTokenService csrfTokens) {
        super(autorizacao);
        this.usuarios = usuarios;
        this.tentativasLogin = tentativasLogin;
        this.csrfTokens = csrfTokens;
    }

    @GetMapping("/auth/session")
    public ResponseEntity<UsuarioDto> sessao(HttpSession session) {
        return SessaoAutenticacao.usuario(session)
                .map(usuario -> ResponseEntity.ok(usuarioDto(usuario)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/security/csrf")
    public ResponseEntity<Map<String, String>> csrf(HttpSession session) {
        return ResponseEntity.ok(Map.of("token", csrfTokens.obterOuCriar(session)));
    }

    @PostMapping("/auth/login")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UsuarioDto.class)))
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpSession session,
            HttpServletRequest httpRequest) {
        String origem = httpRequest.getRemoteAddr();
        if (tentativasLogin.bloqueado(origem)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(
                            Map.of(
                                    "mensagem",
                                    "Muitas tentativas. Aguarde alguns minutos e tente"
                                            + " novamente."));
        }
        return usuarios.autenticar(request.acesso(), request.senha())
                .<ResponseEntity<?>>map(
                        usuario -> {
                            httpRequest.changeSessionId();
                            SessaoAutenticacao.iniciar(session, usuario);
                            csrfTokens.renovar(session);
                            tentativasLogin.limpar(origem);
                            return ResponseEntity.ok(usuarioDto(usuario));
                        })
                .orElseGet(
                        () -> {
                            tentativasLogin.registrarFalha(origem);
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                    .body(Map.of("mensagem", "Acesso ou senha inválidos."));
                        });
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
