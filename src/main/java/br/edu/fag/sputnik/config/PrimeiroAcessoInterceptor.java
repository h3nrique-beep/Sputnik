package br.edu.fag.sputnik.config;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class PrimeiroAcessoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        boolean deveTrocarSenha =
                session != null
                        && SessaoAutenticacao.usuario(session)
                                .map(usuario -> usuario.trocaSenhaObrigatoria())
                                .orElse(false);

        boolean rotaPermitida =
                request.getRequestURI().startsWith("/primeiro-acesso")
                        || request.getRequestURI().equals("/logout")
                        || Set.of(
                                        "/",
                                        "/index.html",
                                        "/api/auth/session",
                                        "/api/auth/logout",
                                        "/api/security/csrf")
                                .contains(request.getRequestURI());
        if (deveTrocarSenha && !rotaPermitida) {
            if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter()
                        .write(
                                "{\"mensagem\":\"Defina sua senha definitiva antes de"
                                        + " continuar.\"}");
                return false;
            }
            response.sendRedirect("/primeiro-acesso");
            return false;
        }
        return true;
    }
}
