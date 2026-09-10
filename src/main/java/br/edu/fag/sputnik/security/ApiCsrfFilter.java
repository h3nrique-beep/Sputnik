package br.edu.fag.sputnik.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/** Exige token anti-CSRF em toda alteração realizada pela API React. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ApiCsrfFilter extends OncePerRequestFilter {
    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private final CsrfTokenService csrfTokens;

    public ApiCsrfFilter(CsrfTokenService csrfTokens) {
        this.csrfTokens = csrfTokens;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (METODOS_SEGUROS.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("X-CSRF-Token");
        if (!csrfTokens.corresponde(request.getSession(false), token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter()
                    .write(
                            "{\"mensagem\":\"Solicitação inválida ou expirada. Atualize a página e"
                                    + " tente novamente.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
