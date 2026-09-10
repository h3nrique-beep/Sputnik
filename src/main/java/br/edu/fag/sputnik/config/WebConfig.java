package br.edu.fag.sputnik.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final PrimeiroAcessoInterceptor primeiroAcessoInterceptor;

    public WebConfig(PrimeiroAcessoInterceptor primeiroAcessoInterceptor) {
        this.primeiroAcessoInterceptor = primeiroAcessoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(primeiroAcessoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/app.css",
                        "/assets/**",
                        "/images/**",
                        "/favicon.ico",
                        "/h2-console/**");
    }
}
