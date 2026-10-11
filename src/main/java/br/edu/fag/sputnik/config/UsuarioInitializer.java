package br.edu.fag.sputnik.config;

import br.edu.fag.sputnik.auth.UsuarioService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Configuration
@Profile("!test")
public class UsuarioInitializer {
    @Value("${sputnik.bootstrap.admin.usuario:admin}")
    private String usuarioAdministrador;

    @Value("${sputnik.bootstrap.admin.senha:}")
    private String senhaAdministrador;

    @Bean
    @Order(1)
    CommandLineRunner usuariosPadrao(UsuarioService usuarios) {
        return args -> {
            usuarios.criarAdministradorInicial(usuarioAdministrador, senhaAdministrador);
            usuarios.migrarEmpresasLegadas();
            usuarios.migrarBalconistaPadrao();
        };
    }
}
