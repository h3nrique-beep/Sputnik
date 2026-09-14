package br.edu.fag.sputnik.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioAutenticado;
import br.edu.fag.sputnik.auth.UsuarioService;
import br.edu.fag.sputnik.domain.Perfil;
import br.edu.fag.sputnik.domain.PermissaoModulo;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:refactor_regression;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "sputnik.saas.self-signup.enabled=true"
        })
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
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
{"clienteId":%d,"veiculoId":%d,"descricao":"Revisão","quilometragemAtual":120,
 "agendamento":"2026-10-01T09:30:00","retornoEm":"2026-11-01","garantiaAte":"2026-12-01"}
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
                        .andExpect(jsonPath("$.ordem.agendamento").value("2026-10-01T09:30:00"))
                        .andExpect(jsonPath("$.ordem.retornoEm").value("2026-11-01"))
                        .andExpect(jsonPath("$.ordem.garantiaAte").value("2026-12-01"))
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
    void orcamentoSoBaixaEstoqueQuandoForAprovado() throws Exception {
        long cliente =
                id(
                        enviar(
                                        "/clientes",
                                        """
{"nome":"Cliente orçamento","cpf":"11111111111","telefone":"4500000001"}
""")
                                .andExpect(status().isCreated()));
        long veiculo =
                id(
                        enviar(
                                        "/veiculos",
                                        """
{"placa":"ORC1A23","marcaModelo":"Carro orçamento","ano":2021,
 "chassi":"CHASSI-ORCAMENTO","quilometragemAtual":200,"clienteId":%d}
"""
                                                .formatted(cliente))
                                .andExpect(status().isCreated()));
        long orcamento =
                id(
                        enviar(
                                        "/ordens",
                                        """
{"clienteId":%d,"veiculoId":%d,"descricao":"Orçamento de revisão",
 "quilometragemAtual":220,"orcamento":true}
"""
                                                .formatted(cliente, veiculo))
                                .andExpect(status().isCreated()));
        long peca = peca();

        enviar("/ordens/" + orcamento + "/itens/peca", "{\"pecaId\":" + peca + ",\"quantidade\":2}")
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/pecas").param("busca", "REF-TESTE").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saldo").value(10));

        enviar("/ordens/" + orcamento + "/aprovar", "{}")
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/ordens/" + orcamento).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordem.status").value("ABERTA"));
        mvc.perform(get("/api/pecas").param("busca", "REF-TESTE").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saldo").value(8));
    }

    @Test
    void financeiroRegistraELiquidaLancamento() throws Exception {
        long lancamento =
                id(
                        enviar(
                                        "/financeiro",
                                        """
{"tipo":"RECEITA","descricao":"Serviço de teste","categoria":"Mão de obra",
 "valor":150.50,"vencimento":"2026-10-10"}
""")
                                .andExpect(status().isCreated()));
        mvc.perform(get("/api/financeiro").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));
        enviar("/financeiro/" + lancamento + "/baixar", "{}")
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/financeiro").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PAGO"));
    }

    @Test
    void onboardingCriaEmpresaComAdminEPlanoDeTeste() throws Exception {
        enviar(
                        "/assinaturas/cadastro",
                        """
{"empresaNome":"Oficina SaaS","slug":"oficina-saas","nomeAdministrador":"Responsável",
 "email":"admin@oficina-saas.test","senha":"SenhaForte#2026"}
""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empresaNome").value("Oficina SaaS"));
        mvc.perform(get("/api/empresa/assinatura").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plano").value("STARTER"))
                .andExpect(jsonPath("$.usuariosAtivos").value(1));
        mvc.perform(get("/api/admin/usuarios").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void empresaDaSessaoIsolaDadosOperacionais() throws Exception {
        SessaoAutenticacao.iniciar(
                session,
                new UsuarioAutenticado(
                        11L, 101L, "Oficina A", "Admin A", Perfil.ADMIN, Set.of(), false));
        token = csrf.obterOuCriar(session);
        long pecaEmpresaA = id(
                enviar(
                                "/pecas",
                                """
{"codigo":"TENANT-A","nome":"Peça da empresa A","fabricante":"Teste",
 "categoria":"Teste","fornecedor":"Teste","localizacao":"A1","preco":10,
 "saldo":1,"estoqueDisponivel":1,"estoqueMinimo":0}
""")
                        .andExpect(status().isCreated()));

        MockHttpSession outraEmpresa = new MockHttpSession();
        SessaoAutenticacao.iniciar(
                outraEmpresa,
                new UsuarioAutenticado(
                        12L, 202L, "Oficina B", "Admin B", Perfil.ADMIN, Set.of(), false));
        mvc.perform(get("/api/pecas").session(outraEmpresa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'TENANT-A')]").isEmpty());
        mvc.perform(
                        post("/api/movimentacoes")
                                .session(outraEmpresa)
                                .header("X-CSRF-Token", csrf.obterOuCriar(outraEmpresa))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"pecaId\":" + pecaEmpresaA + ",\"tipo\":\"SAIDA\",\"quantidade\":1}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/pecas").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'TENANT-A')]").exists());
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
        var reset =
                enviar("/admin/usuarios/" + usuario + "/resetar-senha", "{}")
                        .andExpect(status().isOk())
                        .andReturn();
        String senhaTemporaria =
                json.readTree(reset.getResponse().getContentAsString()).get("senhaTemporaria").asText();
        assertThat(senhaTemporaria).hasSize(20).isNotEqualTo("12345678");
        assertThat(usuarios.autenticar("refatoracao@example.test", senhaTemporaria))
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

    @Test
    void permissaoFinanceiraESeparadaDoRelatorioMensal() throws Exception {
        SessaoAutenticacao.iniciar(
                session,
                new UsuarioAutenticado(
                        2L, "Financeiro", Perfil.BALCONISTA, Set.of(PermissaoModulo.FINANCEIRO), false));
        mvc.perform(get("/api/financeiro").session(session)).andExpect(status().isOk());
        mvc.perform(get("/api/relatorio-mensal").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/api/dashboard").session(session)).andExpect(status().isForbidden());

        SessaoAutenticacao.iniciar(
                session,
                new UsuarioAutenticado(
                        3L,
                        "Relatórios",
                        Perfil.BALCONISTA,
                        Set.of(PermissaoModulo.RELATORIO_MENSAL),
                        false));
        mvc.perform(get("/api/financeiro").session(session)).andExpect(status().isForbidden());
    }
}
