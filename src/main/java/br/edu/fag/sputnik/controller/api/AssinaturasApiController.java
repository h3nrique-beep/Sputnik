package br.edu.fag.sputnik.controller.api;

import br.edu.fag.sputnik.auth.UsuarioService;
import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.controller.api.mapper.ApiMapper;
import br.edu.fag.sputnik.web.CadastroEmpresaForm;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/assinaturas")
public class AssinaturasApiController {
    private final UsuarioService usuarios;
    @Value("${sputnik.saas.self-signup.enabled:false}")
    private boolean cadastroPublicoHabilitado;
    public AssinaturasApiController(UsuarioService usuarios) { this.usuarios = usuarios; }
    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody CadastroEmpresaForm form, HttpSession session) {
        if (!cadastroPublicoHabilitado) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(java.util.Map.of("mensagem", "Cadastro público será liberado após a migração multiempresa dos dados operacionais."));
        }
        try {
            var usuario = usuarios.cadastrarEmpresa(form);
            SessaoAutenticacao.iniciar(session, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.usuarioDto(usuario));
        }
        catch (IllegalArgumentException exception) { return ResponseEntity.badRequest().body(java.util.Map.of("mensagem", exception.getMessage())); }
    }
}
