package br.edu.fag.sputnik.controller;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioService;
import br.edu.fag.sputnik.security.LoginAttemptService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final UsuarioService usuarios;
    private final LoginAttemptService tentativasLogin;

    public AuthController(UsuarioService usuarios, LoginAttemptService tentativasLogin) {
        this.usuarios = usuarios;
        this.tentativasLogin = tentativasLogin;
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        return SessaoAutenticacao.estaAutenticado(session) ? "redirect:/" : "login";
    }

    @PostMapping("/login")
    public String entrar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String senha,
            HttpSession session,
            HttpServletRequest request) {
        String origem = request.getRemoteAddr();
        if (tentativasLogin.bloqueado(origem)) return "redirect:/login?bloqueado";
        return usuarios.autenticar(usuario, senha)
                .map(
                        usuarioAutenticado -> {
                            request.changeSessionId();
                            SessaoAutenticacao.iniciar(session, usuarioAutenticado);
                            tentativasLogin.limpar(origem);
                            return usuarioAutenticado.trocaSenhaObrigatoria()
                                    ? "redirect:/primeiro-acesso"
                                    : "redirect:/inicio";
                        })
                .orElseGet(
                        () -> {
                            tentativasLogin.registrarFalha(origem);
                            return "redirect:/login?erro";
                        });
    }

    @PostMapping("/logout")
    public String sair(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
