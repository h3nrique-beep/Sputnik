package br.edu.fag.sputnik.config;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {
    private static final String PACOTE_DTO = "br.edu.fag.sputnik.controller.api.dto.";

    /**
     * Records de resposta (*Dto) sempre preenchem seus campos; só os anotados com
     * {@code @Schema(nullable = true)} ficam opcionais. Lê a anotação no record porque o swagger a
     * descarta em campos que referenciam outro schema.
     */
    @Bean
    OpenApiCustomizer camposObrigatoriosDosDtos() {
        return openApi ->
                openApi.getComponents()
                        .getSchemas()
                        .forEach(
                                (nome, schema) -> {
                                    // O swagger copia o nullable de um campo para o schema
                                    // referenciado; um tipo nunca é nulo por si só.
                                    schema.setNullable(null);
                                    if (!nome.endsWith("Dto") || schema.getProperties() == null)
                                        return;
                                    List<String> obrigatorios = new ArrayList<>();
                                    for (Object campo : schema.getProperties().keySet())
                                        if (!anuladoNoRecord(nome, (String) campo))
                                            obrigatorios.add((String) campo);
                                    schema.setRequired(obrigatorios);
                                });
    }

    private static boolean anuladoNoRecord(String dto, String campo) {
        try {
            Schema anotacao =
                    Class.forName(PACOTE_DTO + dto)
                            .getDeclaredField(campo)
                            .getAnnotation(Schema.class);
            return anotacao != null && anotacao.nullable();
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }
}
