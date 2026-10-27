package br.edu.fag.sputnik.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioAutenticado;
import br.edu.fag.sputnik.domain.Perfil;
import br.edu.fag.sputnik.security.CsrfTokenService;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Set;

/**
 * Sobe o perfil de produção contra um PostgreSQL real: o Flyway cria o esquema e o Hibernate
 * valida (ddl-auto=validate) que ele corresponde às entidades.
 */
@SpringBootTest(properties = "sputnik.bootstrap.admin.senha=SenhaForte#2026")
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
class PostgresMigracaoIntegrationTest {
    static EmbeddedPostgres postgres;

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CsrfTokenService csrf;
    @Autowired Flyway flyway;

    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registro) throws Exception {
        postgres = EmbeddedPostgres.start();
        registro.add("spring.datasource.url", () -> postgres.getJdbcUrl("postgres", "postgres"));
        registro.add("spring.datasource.username", () -> "postgres");
        registro.add("spring.datasource.password", () -> "");
    }

    @AfterAll
    static void parar() throws Exception {
        postgres.close();
    }

    @Test
    void migracaoAplicadaEFluxoDeEstoqueFunciona() throws Exception {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");

        var session = new MockHttpSession();
        SessaoAutenticacao.iniciar(
                session,
                new UsuarioAutenticado(
                        901L, 901L, "Empresa PG", "Admin PG", Perfil.ADMIN, Set.of(), false));
        long peca =
                json.readTree(
                                enviar(
                                                "/pecas",
                                                session,
                                                """
                                                {"codigo":"PG-001","nome":"Filtro","fabricante":"T",
                                                 "categoria":"T","fornecedor":"T","localizacao":"A1",
                                                 "preco":10,"saldo":3,"estoqueDisponivel":3,
                                                 "estoqueMinimo":2}
                                                """)
                                        .andExpect(status().isCreated())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString())
                        .get("id")
                        .asLong();
        enviar(
                        "/movimentacoes",
                        session,
                        "{\"pecaId\":" + peca + ",\"tipo\":\"SAIDA\",\"quantidade\":2}")
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/pecas").session(session))
                .andExpect(jsonPath("$[0].saldo").value(1));
        mvc.perform(get("/api/reposicao").session(session))
                .andExpect(jsonPath("$.pecas.length()").value(1));
    }

    ResultActions enviar(String rota, MockHttpSession session, String body) throws Exception {
        return mvc.perform(
                post("/api" + rota)
                        .session(session)
                        .header("X-CSRF-Token", csrf.obterOuCriar(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }
}
