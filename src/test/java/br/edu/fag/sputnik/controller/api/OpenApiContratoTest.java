package br.edu.fag.sputnik.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Mantém frontend/openapi.json igual à API. Se a API mudar, o teste regrava o arquivo e falha:
 * rode {@code npm run api:types} no frontend e corrija o que o TypeScript apontar.
 */
@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:openapi;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "springdoc.api-docs.enabled=true"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiContratoTest {
    static final Path CONTRATO = Path.of("frontend", "openapi.json");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void contratoDoFrontendEstaAtualizado() throws Exception {
        var bruto = mvc.perform(get("/v3/api-docs")).andReturn().getResponse();
        var arvore = json.readTree(bruto.getContentAsByteArray());
        // O servidor muda com a porta do teste; não faz parte do contrato.
        ((com.fasterxml.jackson.databind.node.ObjectNode) arvore).remove("servers");
        String atual =
                json.copy()
                                .enable(SerializationFeature.INDENT_OUTPUT)
                                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                                .writeValueAsString(json.treeToValue(arvore, Object.class))
                        + "\n";
        String salvo =
                Files.exists(CONTRATO) ? Files.readString(CONTRATO, StandardCharsets.UTF_8) : "";
        if (!atual.equals(salvo)) Files.writeString(CONTRATO, atual, StandardCharsets.UTF_8);
        assertThat(salvo)
                .as("frontend/openapi.json estava desatualizado e foi regravado; rode npm run"
                        + " api:types e confira o TypeScript")
                .isEqualTo(atual);
    }
}
