package br.edu.fag.sputnik.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Limita tentativas repetidas de senha por origem, reduzindo força bruta. */
@Service
public class LoginAttemptService {
    private static final int MAX_TENTATIVAS = 5;
    private static final Duration JANELA = Duration.ofMinutes(10);

    private final ConcurrentHashMap<String, Tentativas> tentativasPorOrigem =
            new ConcurrentHashMap<>();
    private final AtomicLong ultimaLimpeza = new AtomicLong();

    public boolean bloqueado(String origem) {
        Tentativas tentativas = tentativasPorOrigem.get(origem);
        if (tentativas == null) return false;
        if (Instant.now().isAfter(tentativas.inicio().plus(JANELA))) {
            tentativasPorOrigem.remove(origem, tentativas);
            return false;
        }
        return tentativas.quantidade() >= MAX_TENTATIVAS;
    }

    public void registrarFalha(String origem) {
        limparExpiradosQuandoNecessario();
        tentativasPorOrigem.compute(
                origem,
                (chave, atual) -> {
                    Instant agora = Instant.now();
                    if (atual == null || agora.isAfter(atual.inicio().plus(JANELA))) {
                        return new Tentativas(1, agora);
                    }
                    return new Tentativas(atual.quantidade() + 1, atual.inicio());
                });
    }

    public void limpar(String origem) {
        tentativasPorOrigem.remove(origem);
    }

    /** Descarta janelas vencidas para que origens que nunca voltam não acumulem memória. */
    private void limparExpiradosQuandoNecessario() {
        long agora = System.currentTimeMillis();
        long ultima = ultimaLimpeza.get();
        if (agora - ultima < JANELA.toMillis() || !ultimaLimpeza.compareAndSet(ultima, agora)) {
            return;
        }
        Instant limite = Instant.now().minus(JANELA);
        tentativasPorOrigem.values().removeIf(tentativas -> tentativas.inicio().isBefore(limite));
    }

    private record Tentativas(int quantidade, Instant inicio) {}
}
