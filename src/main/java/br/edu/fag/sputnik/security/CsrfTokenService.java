package br.edu.fag.sputnik.security;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/** Gera e mantém um token anti-CSRF exclusivo para cada sessão do navegador. */
@Service
public class CsrfTokenService {
    public static final String ATRIBUTO_TOKEN = "csrfToken";

    private final SecureRandom aleatorio = new SecureRandom();

    public String obterOuCriar(HttpSession session) {
        Object tokenExistente = session.getAttribute(ATRIBUTO_TOKEN);
        if (tokenExistente instanceof String token) return token;

        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(ATRIBUTO_TOKEN, token);
        return token;
    }

    public String renovar(HttpSession session) {
        session.removeAttribute(ATRIBUTO_TOKEN);
        return obterOuCriar(session);
    }

    public boolean corresponde(HttpSession session, String tokenRecebido) {
        if (session == null || tokenRecebido == null || tokenRecebido.isBlank()) return false;
        Object tokenEsperado = session.getAttribute(ATRIBUTO_TOKEN);
        if (!(tokenEsperado instanceof String esperado)) return false;
        return constanteIgual(esperado, tokenRecebido);
    }

    private boolean constanteIgual(String esperado, String recebido) {
        byte[] a = esperado.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] b = recebido.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(a, b);
    }
}
