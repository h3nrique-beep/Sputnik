package br.edu.fag.sputnik.controller.api;

import static br.edu.fag.sputnik.controller.api.mapper.ApiMapper.usuarioSistemaDto;

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
    public ResponseEntity<?> usuarios(HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        return ResponseEntity.ok(
                usuarios.listar().stream().map(ApiMapper::usuarioSistemaDto).toList());
    }

    @PostMapping("/admin/usuarios")
    public ResponseEntity<?> cadastrarUsuario(
            @Valid @RequestBody UsuarioCadastroForm form, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(usuarioSistemaDto(usuarios.cadastrar(form)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }

    @PutMapping("/admin/usuarios/{id}/permissoes")
    public ResponseEntity<?> atualizarPermissoes(
            @PathVariable Long id, @RequestBody UsuarioPermissoesForm form, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        try {
            usuarios.atualizarPermissoes(id, form.getPermissoes());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }

    @PostMapping("/admin/usuarios/{id}/resetar-senha")
    public ResponseEntity<?> resetarSenha(@PathVariable Long id, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        try {
            usuarios.resetarSenhaBalconista(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }

    @DeleteMapping("/admin/usuarios/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Long id, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) return proibido();
        try {
            usuarios.excluirBalconista(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }
}
