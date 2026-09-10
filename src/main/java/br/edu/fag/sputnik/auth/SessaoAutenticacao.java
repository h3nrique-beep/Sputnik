package br.edu.fag.sputnik.auth;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;

public final class SessaoAutenticacao {
    public static final String USUARIO_AUTENTICADO = "usuarioAutenticado";

    private SessaoAutenticacao() {}

    public static void iniciar(HttpSession session, UsuarioAutenticado usuario) {
        session.setAttribute(USUARIO_AUTENTICADO, usuario);
    }

    public static Optional<UsuarioAutenticado> usuario(HttpSession session) {
        Object atributo = session.getAttribute(USUARIO_AUTENTICADO);
        return atributo instanceof UsuarioAutenticado usuario
                ? Optional.of(usuario)
                : Optional.empty();
    }

    public static boolean estaAutenticado(HttpSession session) {
        return usuario(session).isPresent();
    }
}
