package br.edu.fag.sputnik.controller.api;

import java.util.List;
import br.edu.fag.sputnik.controller.api.dto.UsuarioSistemaDto;
import static br.edu.fag.sputnik.controller.api.mapper.ApiMapper.usuarioSistemaDto;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioService;
import br.edu.fag.sputnik.controller.api.mapper.ApiMapper;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.web.UsuarioCadastroForm;
import br.edu.fag.sputnik.web.UsuarioPermissoesForm;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsuariosApiController extends ApiSupport {
    private final UsuarioService usuarios;

    public UsuariosApiController(AutorizacaoService autorizacao, UsuarioService usuarios) {
        super(autorizacao);
        this.usuarios = usuarios;
    }

    @GetMapping("/admin/usuarios")
    public ResponseEntity<List<UsuarioSistemaDto>> usuarios(HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        return ResponseEntity.ok(
                usuarios.listar(empresaId(session)).stream()
                        .map(ApiMapper::usuarioSistemaDto)
                        .toList());
    }

    @PostMapping("/admin/usuarios")
    public ResponseEntity<UsuarioSistemaDto> cadastrarUsuario(
            @Valid @RequestBody UsuarioCadastroForm form, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioSistemaDto(usuarios.cadastrar(form, empresaId(session))));
    }

    @PutMapping("/admin/usuarios/{id}/permissoes")
    public ResponseEntity<Void> atualizarPermissoes(
            @PathVariable Long id, @RequestBody UsuarioPermissoesForm form, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        usuarios.atualizarPermissoes(id, form.getPermissoes(), empresaId(session));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/usuarios/{id}/resetar-senha")
    public ResponseEntity<Map<String, String>> resetarSenha(@PathVariable Long id, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        return ResponseEntity.ok(
                Map.of(
                        "senhaTemporaria",
                        usuarios.resetarSenhaBalconista(id, empresaId(session))));
    }

    @DeleteMapping("/admin/usuarios/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        usuarios.excluirBalconista(id, empresaId(session));
        return ResponseEntity.noContent().build();
    }

    private Long empresaId(HttpSession session) {
        return SessaoAutenticacao.usuario(session).map(usuario -> usuario.empresaId()).orElse(null);
    }
}
