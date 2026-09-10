package br.edu.fag.sputnik.service;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioAutenticado;
import br.edu.fag.sputnik.domain.Perfil;
import br.edu.fag.sputnik.domain.PermissaoModulo;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import java.util.Optional;

/** Centraliza as regras de autenticação e autorização baseadas na sessão. */
@Service
public class AutorizacaoService {

    public Optional<UsuarioAutenticado> usuarioAtual(HttpSession session) {
        return SessaoAutenticacao.usuario(session);
    }

    public boolean estaLogado(HttpSession session) {
        return usuarioAtual(session).isPresent();
    }

    public boolean ehAdministrador(HttpSession session) {
        return usuarioAtual(session).map(usuario -> usuario.perfil() == Perfil.ADMIN).orElse(false);
    }

    public boolean temAcesso(HttpSession session, PermissaoModulo modulo) {
        return usuarioAtual(session)
                .map(
                        usuario ->
                                usuario.perfil() == Perfil.ADMIN
                                        || usuario.permissoes().contains(modulo))
                .orElse(false);
    }

    public String redirecionarSemAcesso(HttpSession session) {
        return estaLogado(session) ? "redirect:/inicio?acessoNegado" : "redirect:/login";
    }
}
