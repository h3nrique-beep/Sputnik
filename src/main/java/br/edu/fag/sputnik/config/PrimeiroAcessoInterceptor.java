package br.edu.fag.sputnik.config;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
                        || request.getRequestURI().startsWith("/logout");
        if (deveTrocarSenha && !rotaPermitida) {
            response.sendRedirect("/primeiro-acesso");
            return false;
        }
        return true;
    }
}
