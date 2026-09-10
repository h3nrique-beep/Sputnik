package br.edu.fag.sputnik.config;

import br.edu.fag.sputnik.auth.UsuarioService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioInitializer {

    @Bean
    CommandLineRunner usuariosPadrao(UsuarioService usuarios) {
        return args -> {
            usuarios.criarPadraoSeNecessario();
            usuarios.migrarBalconistaPadrao();
        };
    }
}
