package br.edu.fag.sputnik.controller.api;

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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Set;
import java.util.UUID;

/** Regressões da revisão de segurança de 11/10/2026. */
@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:revisao;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.open-in-view=false",
            "sputnik.saas.self-signup.enabled=true"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RevisaoSegurancaIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CsrfTokenService csrf;

    MockHttpSession sessao(long empresa) {
        var session = new MockHttpSession();
        SessaoAutenticacao.iniciar(
                session,
                new UsuarioAutenticado(
                        empresa, empresa, "Empresa teste", "Admin teste", Perfil.ADMIN, Set.of(),
                        false));
        return session;
    }

    ResultActions enviar(String rota, MockHttpSession session, String body) throws Exception {
        return mvc.perform(
                post("/api" + rota)
                        .session(session)
                        .header("X-CSRF-Token", csrf.obterOuCriar(session))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }

    long id(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    @Test
    void ordemComVeiculoSemProprietarioEhRecusadaSemErroInterno() throws Exception {
        var session = sessao(901L);
        long cliente =
                id(
                        enviar(
                                        "/clientes",
                                        session,
                                        """
                                        {"nome":"Pessoa","cpf":"12345678900","telefone":"45999990000"}
                                        """)
                                .andExpect(status().isCreated()));
        long veiculo =
                id(
                        enviar(
                                        "/veiculos",
                                        session,
                                        """
                                        {"placa":"SEM1D00","marcaModelo":"Sem dono","ano":2020,
                                         "chassi":"CHASSI-SEM-DONO","quilometragemAtual":10}
                                        """)
                                .andExpect(status().isCreated()));

        enviar(
                        "/ordens",
                        session,
                        """
                        {"clienteId":%d,"veiculoId":%d,"descricao":"Revisão","quilometragemAtual":20}
                        """
                                .formatted(cliente, veiculo))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.mensagem")
                                .value("O carro selecionado não pertence à pessoa cadastrada."));
    }

    @Test
    void cadastroPublicoRenovaSessaoETokenAntiCsrf() throws Exception {
        var session = new MockHttpSession();
        String tokenAnterior = csrf.obterOuCriar(session);
        String sessaoAnterior = session.getId();
        String sufixo = UUID.randomUUID().toString().substring(0, 8);

        mvc.perform(
                        post("/api/assinaturas/cadastro")
                                .session(session)
                                .header("X-CSRF-Token", tokenAnterior)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"empresaNome":"Oficina %s","slug":"oficina-%s",
                                         "nomeAdministrador":"Responsável",
                                         "email":"dono-%s@example.test","senha":"SenhaForte#2026"}
                                        """
                                                .formatted(sufixo, sufixo, sufixo)))
                .andExpect(status().isCreated());

        assertThat(session.getId()).isNotEqualTo(sessaoAnterior);
        assertThat(csrf.obterOuCriar(session)).isNotEqualTo(tokenAnterior);
        mvc.perform(get("/api/auth/session").session(session)).andExpect(status().isOk());
    }
}
