package br.edu.fag.sputnik.auth;

import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class SessaoAutenticacao {
    public static final String USUARIO_AUTENTICADO = "usuarioAutenticado";
    private static final Map<HttpSession, Long> SESSOES = new WeakHashMap<>();

    private SessaoAutenticacao() {}

    public static synchronized void iniciar(HttpSession session, UsuarioAutenticado usuario) {
        session.setAttribute(USUARIO_AUTENTICADO, usuario);
        SESSOES.put(session, usuario.usuarioId());
    }

    /** Remove a autenticação de sessões abertas quando uma conta ou senha é alterada. */
    public static synchronized void revogar(Long usuarioId) {
        var iterator = SESSOES.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (!entry.getValue().equals(usuarioId)) continue;
            try {
                entry.getKey().removeAttribute(USUARIO_AUTENTICADO);
            } catch (IllegalStateException ignored) {
                /* Sessão já expirada pelo contêiner. */
            }
            iterator.remove();
        }
    }

    public static Optional<UsuarioAutenticado> usuario(HttpSession session) {
        if (session == null) return Optional.empty();
        Object atributo = session.getAttribute(USUARIO_AUTENTICADO);
        return atributo instanceof UsuarioAutenticado usuario
                ? Optional.of(usuario)
                : Optional.empty();
    }

    public static boolean estaAutenticado(HttpSession session) {
        return usuario(session).isPresent();
    }
}
