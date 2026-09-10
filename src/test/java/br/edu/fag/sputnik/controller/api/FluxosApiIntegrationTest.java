package br.edu.fag.sputnik.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioAutenticado;
import br.edu.fag.sputnik.auth.UsuarioService;
import br.edu.fag.sputnik.domain.Perfil;
import br.edu.fag.sputnik.security.CsrfTokenService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:refactor_regression;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
@AutoConfigureMockMvc
@Transactional
class FluxosApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CsrfTokenService csrf;
    @Autowired UsuarioService usuarios;
    MockHttpSession session;
    String token;

    @BeforeEach
    void autenticar() {
        session = new MockHttpSession();
        SessaoAutenticacao.iniciar(
                session, new UsuarioAutenticado(1L, "Teste", Perfil.ADMIN, Set.of(), false));
        token = csrf.obterOuCriar(session);
    }

    ResultActions enviar(String rota, String body) throws Exception {
        return mvc.perform(
                post("/api" + rota)
                        .session(session)
                        .header("X-CSRF-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }

    long id(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    long peca() throws Exception {
        return id(
                enviar(
                                "/pecas",
                                """
{"codigo":"REF-TESTE","nome":"Filtro de teste","fabricante":"Teste",
 "categoria":"Filtro","fornecedor":"Teste","localizacao":"A1",
 "preco":25,"saldo":10,"estoqueDisponivel":10,"estoqueMinimo":2}
""")
                        .andExpect(status().isCreated()));
    }

    @Test
    void cadastroAberturaConsumoETotalDaOsContinuamIntegrados() throws Exception {
        long cliente =
                id(
                        enviar(
                                        "/clientes",
                                        """
{"nome":"Cliente de teste","cpf":"00000000000","telefone":"4500000000"}
""")
                                .andExpect(status().isCreated()));
        long veiculo =
                id(
                        enviar(
                                        "/veiculos",
                                        """
{"placa":"REF1A23","marcaModelo":"Carro teste","ano":2020,
 "chassi":"CHASSI-REGRESSAO","quilometragemAtual":100,"clienteId":%d}
"""
                                                .formatted(cliente))
                                .andExpect(status().isCreated()));
        long ordem =
                id(
                        enviar(
                                        "/ordens",
                                        """
{"clienteId":%d,"veiculoId":%d,"descricao":"Revisão","quilometragemAtual":120}
"""
                                                .formatted(cliente, veiculo))
                                .andExpect(status().isCreated()));
        long peca = peca();
        enviar("/ordens/" + ordem + "/itens/peca", "{\"pecaId\":" + peca + ",\"quantidade\":2}")
                .andExpect(status().isNoContent());
        enviar(
                        "/ordens/" + ordem + "/itens/servico",
                        "{\"descricao\":\"Troca de filtro\",\"valor\":80}")
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/pecas").param("busca", "REF-TESTE").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saldo").value(8));
        var detail =
                mvc.perform(get("/api/ordens/" + ordem).session(session))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ordem.status").value("EM_ANDAMENTO"))
                        .andExpect(jsonPath("$.ordem.quilometragem").value(120))
                        .andReturn();
        var itens = json.readTree(detail.getResponse().getContentAsString()).get("itens");
        assertThat(itens).hasSize(2);
        assertThat(
                        itens.get(0)
                                .get("total")
                                .decimalValue()
                                .add(itens.get(1).get("total").decimalValue()))
                .isEqualByComparingTo("130.00");
    }

    @Test
    void saidaSemSaldoNaoAlteraEstoque() throws Exception {
        long peca = peca();
        enviar("/movimentacoes", "{\"pecaId\":" + peca + ",\"tipo\":\"SAIDA\",\"quantidade\":11}")
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/pecas").param("busca", "REF-TESTE").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saldo").value(10));
    }

    @Test
    void gestaoDeUsuariosPreservaPermissoesEResetDeSenha() throws Exception {
        long usuario =
                id(
                        enviar(
                                        "/admin/usuarios",
                                        """
{"nome":"Usuário teste","email":"refatoracao@example.test","senha":"temporaria","permissoes":["PECAS"]}
""")
                                .andExpect(status().isCreated()));
        mvc.perform(
                        put("/api/admin/usuarios/" + usuario + "/permissoes")
                                .session(session)
                                .header("X-CSRF-Token", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"permissoes\":[\"ORDENS\"]}"))
                .andExpect(status().isNoContent());
        enviar("/admin/usuarios/" + usuario + "/resetar-senha", "{}")
                .andExpect(status().isNoContent());
        assertThat(usuarios.autenticar("refatoracao@example.test", "12345678"))
                .get()
                .extracting(UsuarioAutenticado::trocaSenhaObrigatoria)
                .isEqualTo(true);
    }

    @Test
    void permissoesECsrfContinuamProtegendoRotasExtraidas() throws Exception {
        mvc.perform(
                        post("/api/clientes")
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isForbidden());
        SessaoAutenticacao.iniciar(
                session,
                new UsuarioAutenticado(2L, "Restrito", Perfil.BALCONISTA, Set.of(), false));
        for (String rota :
                new String[] {
                    "/pecas",
                    "/clientes",
                    "/veiculos",
                    "/ordens",
                    "/relatorio-mensal",
                    "/reposicao",
                    "/admin/usuarios",
                    "/admin/desenvolvedor/backups"
                }) {
            mvc.perform(get("/api" + rota).session(session)).andExpect(status().isForbidden());
        }
    }
}
