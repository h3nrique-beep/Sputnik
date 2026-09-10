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
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Evita sobrecarga acidental ou automatizada nos endpoints da API. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ApiRequestRateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUISICOES = 180;
    private static final Duration JANELA = Duration.ofMinutes(1);
    private final ConcurrentHashMap<String, Janela> porOrigem = new ConcurrentHashMap<>();
    private final AtomicLong ultimaLimpeza = new AtomicLong();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        limparExpiradosQuandoNecessario();
        String origem = request.getRemoteAddr();
        if (!permitido(origem)) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter()
                    .write(
                            "{\"mensagem\":\"Muitas solicitações. Aguarde um minuto e tente"
                                    + " novamente.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean permitido(String origem) {
        Instant agora = Instant.now();
        Janela janela =
                porOrigem.compute(
                        origem,
                        (chave, atual) -> {
                            if (atual == null || agora.isAfter(atual.inicio().plus(JANELA)))
                                return new Janela(1, agora);
                            return new Janela(atual.quantidade() + 1, atual.inicio());
                        });
        return janela.quantidade() <= MAX_REQUISICOES;
    }

    private void limparExpiradosQuandoNecessario() {
        long agora = System.currentTimeMillis();
        long ultima = ultimaLimpeza.get();
        if (agora - ultima < Duration.ofMinutes(5).toMillis()
                || !ultimaLimpeza.compareAndSet(ultima, agora)) return;
        Instant limite = Instant.now().minus(JANELA);
        porOrigem.entrySet().removeIf(entry -> entry.getValue().inicio().isBefore(limite));
    }

    private record Janela(int quantidade, Instant inicio) {}
}
