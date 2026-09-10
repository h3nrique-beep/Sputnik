package br.edu.fag.sputnik.config;

import br.edu.fag.sputnik.repository.PecaRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Completa o estoque disponível dos registros criados antes desse campo existir. */
@Configuration
public class DisponivelMigration {

    @Bean
    CommandLineRunner preencherEstoqueDisponivel(PecaRepository pecas) {
        return args ->
                pecas.findAll()
                        .forEach(
                                peca -> {
                                    if (!peca.temEstoqueDisponivel()) {
                                        peca.setEstoqueDisponivel(peca.getSaldo());
                                        pecas.save(peca);
                                    }
                                });
    }
}
