package br.edu.fag.sputnik.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.support.RequestDataValueProcessor;

/** Integra o token da sessão aos formulários Thymeleaf com th:action. */
@Configuration
public class FormSecurityConfig {
    @Bean
    RequestDataValueProcessor requestDataValueProcessor(CsrfTokenService tokens) {
        return new RequestDataValueProcessor() {
            public String processAction(HttpServletRequest request, String action, String method) { return action; }
            public String processFormFieldValue(HttpServletRequest request, String name, String value, String type) { return value; }
            public Map<String, String> getExtraHiddenFields(HttpServletRequest request) {
                return Map.of("_csrf", tokens.obterOuCriar(request.getSession()));
            }
            public String processUrl(HttpServletRequest request, String url) { return url; }
        };
    }
}
