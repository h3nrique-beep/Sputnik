package br.edu.fag.sputnik.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/** Limita tentativas repetidas de senha por origem, reduzindo força bruta. */
@Service
public class LoginAttemptService {
    private static final int MAX_TENTATIVAS = 5;
    private static final Duration JANELA = Duration.ofMinutes(10);

    private final ConcurrentHashMap<String, Tentativas> tentativasPorOrigem =
            new ConcurrentHashMap<>();

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

    private record Tentativas(int quantidade, Instant inicio) {}
}
