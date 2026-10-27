package br.edu.fag.sputnik.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioAutenticado;
import br.edu.fag.sputnik.domain.Perfil;
import br.edu.fag.sputnik.repository.ClienteRepository;
import br.edu.fag.sputnik.security.CsrfTokenService;
import br.edu.fag.sputnik.tenant.EmpresaContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Sem @Transactional: reproduz o ciclo de transações das requisições reais. */
@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:auditoria;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.open-in-view=false"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditoriaApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CsrfTokenService csrf;
    @Autowired ClienteRepository clientes;
    @Autowired br.edu.fag.sputnik.auth.UsuarioService usuarios;
    @Autowired br.edu.fag.sputnik.service.PecasService pecasService;

    long peca(MockHttpSession session, int saldo, int disponivel) throws Exception {
        return id(
                enviar(
                                "/pecas",
                                session,
                                """
{"codigo":"%s","nome":"Peça auditoria","fabricante":"Teste","categoria":"Teste",
 "fornecedor":"Teste","localizacao":"A1","preco":10,"saldo":%d,
 "estoqueDisponivel":%d,"estoqueMinimo":2}
"""
                                        .formatted(UUID.randomUUID(), saldo, disponivel))
                        .andExpect(status().isCreated()));
    }

    MockHttpSession sessao(long empresa, boolean trocarSenha) {
        var session = new MockHttpSession();
        SessaoAutenticacao.iniciar(
                session,
                new UsuarioAutenticado(
                        empresa,
                        empresa,
                        "Empresa teste",
                        "Admin teste",
                        Perfil.ADMIN,
                        Set.of(),
                        trocarSenha));
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
    void listasIsolamEmpresasSemTransacaoExterna() throws Exception {
        var a = sessao(801L, false);
        var b = sessao(802L, false);
        String nome = "Privado-" + UUID.randomUUID();
        enviar(
                        "/clientes",
                        a,
                        """
                        {"nome":"%s","cpf":"12345678900","telefone":"4500000000"}
                        """
                                .formatted(nome))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/clientes").param("busca", nome).session(a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/clientes").param("busca", nome).session(b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        assertThat(EmpresaContext.empresaId()).isNull();
    }

    @Test
    void cadastroNaoAceitaIdentidadeOuEmpresaEnviadasPeloCliente() throws Exception {
        var a = sessao(811L, false);
        long original =
                id(
                        enviar(
                                        "/clientes",
                                        a,
                                        """
{"nome":"Original","cpf":"11111111111","telefone":"4500000000"}
""")
                                .andExpect(status().isCreated()));
        long criado =
                id(
                        enviar(
                                        "/clientes",
                                        sessao(812L, false),
                                        """
{"id":%d,"empresaId":811,"nome":"Intruso","cpf":"22222222222","telefone":"4500000001"}
"""
                                                .formatted(original))
                                .andExpect(status().isCreated()));
        assertThat(criado).isNotEqualTo(original);
        assertThat(clientes.findById(original).orElseThrow().getNome()).isEqualTo("Original");
        assertThat(clientes.findById(criado).orElseThrow().getEmpresaId()).isEqualTo(812L);
    }

    @Test
    void movimentacaoPreservaParteIndisponivelDoEstoque() throws Exception {
        var session = sessao(821L, false);
        long peca =
                id(
                        enviar(
                                        "/pecas",
                                        session,
                                        """
{"codigo":"%s","nome":"Peça","fabricante":"Teste","categoria":"Teste",
 "fornecedor":"Teste","localizacao":"A1","preco":10,"saldo":10,
 "estoqueDisponivel":4,"estoqueMinimo":2}
"""
                                                .formatted(UUID.randomUUID()))
                                .andExpect(status().isCreated()));
        enviar(
                        "/movimentacoes",
                        session,
                        "{\"pecaId\":" + peca + ",\"tipo\":\"ENTRADA\",\"quantidade\":2}")
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/pecas").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saldo").value(12))
                .andExpect(jsonPath("$[0].disponivel").value(6));
        enviar(
                        "/movimentacoes",
                        session,
                        "{\"pecaId\":" + peca + ",\"tipo\":\"SAIDA\",\"quantidade\":7}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void administracaoNaoAlcancaUsuarioDeOutraEmpresa() throws Exception {
        var cadastro = new br.edu.fag.sputnik.web.UsuarioCadastroForm();
        cadastro.setNome("Conta de outra empresa");
        cadastro.setEmail("adm-" + UUID.randomUUID() + "@example.test");
        cadastro.setSenha("Temporaria#2026");
        var conta = usuarios.cadastrar(cadastro);
        var empresa = new br.edu.fag.sputnik.web.CadastroEmpresaForm();
        empresa.setEmpresaNome("Outra oficina");
        empresa.setSlug("adm-" + UUID.randomUUID());
        empresa.setNomeAdministrador("Admin de outra oficina");
        empresa.setEmail("admin-" + UUID.randomUUID() + "@example.test");
        empresa.setSenha("SenhaForte#2026");
        var administrador = usuarios.cadastrarEmpresa(empresa);
        var session = new MockHttpSession();
        SessaoAutenticacao.iniciar(session, administrador);
        mvc.perform(get("/api/admin/usuarios").session(session))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$[*].id",
                                Matchers.not(Matchers.hasItem(conta.getId().intValue()))));
        enviar("/admin/usuarios/" + conta.getId() + "/resetar-senha", session, "{}")
                .andExpect(status().isBadRequest());
        assertThat(usuarios.autenticar(cadastro.getEmail(), cadastro.getSenha())).isPresent();
    }

    @Test
    void primeiroAcessoPermiteConsultarSessaoESairMasBloqueiaOperacao() throws Exception {
        var session = sessao(831L, true);
        mvc.perform(get("/api/auth/session").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        mvc.perform(
                        post("/primeiro-acesso")
                                .session(session)
                                .param("_csrf", csrf.obterOuCriar(session)))
                .andExpect(status().isOk())
                .andExpect(view().name("primeiro-acesso"));
        mvc.perform(get("/api/clientes").session(session)).andExpect(status().isForbidden());
        enviar("/auth/logout", session, "{}").andExpect(status().isNoContent());
    }

    @Test
    void referenciaInexistenteRetornaErroDeEntradaEmJson() throws Exception {
        enviar(
                        "/veiculos",
                        sessao(841L, false),
                        """
                        {"placa":"AUD1A23","marcaModelo":"Teste","ano":2020,
                         "chassi":"AUDITORIA","quilometragemAtual":0,"clienteId":999999999}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void saidasSimultaneasNaoVendemAMesmaUnidade() throws Exception {
        var session = sessao(851L, false);
        long peca = peca(session, 1, 1);
        var inicio = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        Callable<Integer> retirar =
                () -> {
                    inicio.await();
                    return enviar(
                                    "/movimentacoes",
                                    session,
                                    "{\"pecaId\":" + peca + ",\"tipo\":\"SAIDA\",\"quantidade\":1}")
                            .andReturn()
                            .getResponse()
                            .getStatus();
                };
        try {
            var a = executor.submit(retirar);
            var b = executor.submit(retirar);
            inicio.countDown();
            assertThat(List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(204, 400);
        } finally {
            executor.shutdownNow();
        }
        mvc.perform(get("/api/pecas").session(session))
                .andExpect(jsonPath("$[0].saldo").value(0))
                .andExpect(jsonPath("$[0].disponivel").value(0));
    }

    @Test
    void formularioDePrimeiroAcessoRecebeCsrfEApiIgnoraEmpresaForjada() throws Exception {
        var anonymous = new MockHttpSession();
        mvc.perform(get("/login").session(anonymous))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/index.html"));
        mvc.perform(post("/login").session(anonymous)).andExpect(status().isForbidden());
        mvc.perform(get("/primeiro-acesso").session(sessao(861L, true)))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("name=\"_csrf\"")));
        var session = sessao(861L, false);
        enviar(
                        "/clientes",
                        session,
                        """
                        {"nome":"Cliente forjado","cpf":"33333333333",
                         "telefone":"4500000000","empresaId":801}
                        """)
                .andExpect(status().isCreated());
        mvc.perform(get("/api/clientes").session(session).param("busca", "Cliente forjado"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void reposicaoExportaCsvSomenteParaQuemTemPermissao() throws Exception {
        mvc.perform(get("/api/reposicao.csv").session(sessao(881L, false)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(
                        header().string("Content-Disposition", Matchers.containsString(".csv")));
        mvc.perform(get("/api/reposicao.csv").session(new MockHttpSession()))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorDeOutraEmpresaNaoAcessaBackupGlobal() throws Exception {
        var session = sessao(871L, false);
        for (String rota :
                List.of(
                        "/admin/desenvolvedor",
                        "/admin/desenvolvedor/backups",
                        "/admin/desenvolvedor/backups/sputnik-teste.zip")) {
            mvc.perform(get("/api" + rota).session(session)).andExpect(status().isForbidden());
        }
        enviar("/admin/desenvolvedor/backups", session, "{}").andExpect(status().isForbidden());
    }

    @Test
    void jsonInvalidoEEstoqueInconsistenteSaoRejeitados() throws Exception {
        var session = sessao(881L, false);
        enviar("/pecas", session, "{")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
        enviar(
                        "/pecas",
                        session,
                        """
{"codigo":"INVALIDO","nome":"Peça","fabricante":"Teste","categoria":"Teste",
 "fornecedor":"Teste","localizacao":"A1","preco":10,"saldo":1,"estoqueDisponivel":2}
""")
                .andExpect(status().isBadRequest());
    }

    @Test
    void limitesDeVeiculoMovimentacaoEParametrosInvalidosRetornam400() throws Exception {
        var session = sessao(883L, false);
        enviar(
                        "/veiculos",
                        session,
                        """
                        {"placa":"VAL1A23","marcaModelo":"Teste","ano":2020,"chassi":"VALIDACAO",
                         "quilometragemAtual":-1}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
        enviar(
                        "/veiculos",
                        session,
                        """
                        {"placa":"VAL1A23","marcaModelo":"Teste","ano":1800,"chassi":"VALIDACAO",
                         "quilometragemAtual":0}
                        """)
                .andExpect(status().isBadRequest());
        long piece = peca(session, 5, 5);
        enviar(
                        "/movimentacoes",
                        session,
                        "{\"pecaId\":"
                                + piece
                                + ",\"tipo\":\"ENTRADA\",\"quantidade\":1,\"observacao\":\""
                                + "x".repeat(256)
                                + "\"}")
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/pecas").session(session)).andExpect(jsonPath("$[0].saldo").value(5));
        mvc.perform(get("/api/ordens/nao-numerico").session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void revogacaoDePermissoesESenhaRemoveAutenticacoesAbertas() {
        var form = new br.edu.fag.sputnik.web.UsuarioCadastroForm();
        form.setNome("Operador auditoria");
        form.setEmail("auditoria-" + UUID.randomUUID() + "@example.test");
        form.setSenha("Temporaria#2026");
        var usuario = usuarios.cadastrar(form);
        var conta = usuarios.autenticar(form.getEmail(), form.getSenha()).orElseThrow();
        var a = new MockHttpSession();
        var b = new MockHttpSession();
        SessaoAutenticacao.iniciar(a, conta);
        SessaoAutenticacao.iniciar(b, conta);
        usuarios.atualizarPermissoes(usuario.getId(), Set.of(), conta.empresaId());
        assertThat(SessaoAutenticacao.usuario(a)).isEmpty();
        assertThat(SessaoAutenticacao.usuario(b)).isEmpty();
        SessaoAutenticacao.iniciar(a, conta);
        usuarios.resetarSenhaBalconista(usuario.getId(), conta.empresaId());
        assertThat(SessaoAutenticacao.usuario(a)).isEmpty();
        SessaoAutenticacao.iniciar(a, conta);
        usuarios.excluirBalconista(usuario.getId(), conta.empresaId());
        assertThat(SessaoAutenticacao.usuario(a)).isEmpty();
    }

    @Test
    void orcamentoComPecaRepetidaBaixaTodosItensEIsolaRanking() throws Exception {
        var session = sessao(891L, false);
        long cliente =
                id(
                        enviar(
                                        "/clientes",
                                        session,
                                        """
{"nome":"Orçamento auditoria","cpf":"44444444444","telefone":"4500000000"}
""")
                                .andExpect(status().isCreated()));
        long veiculo =
                id(
                        enviar(
                                        "/veiculos",
                                        session,
                                        """
{"placa":"REP1A23","marcaModelo":"Teste","ano":2020,"chassi":"REPETIDA",
 "quilometragemAtual":0,"clienteId":%d}
"""
                                                .formatted(cliente))
                                .andExpect(status().isCreated()));
        long ordem =
                id(
                        enviar(
                                        "/ordens",
                                        session,
                                        """
{"clienteId":%d,"veiculoId":%d,"descricao":"Revisão","quilometragemAtual":0,"orcamento":true}
"""
                                                .formatted(cliente, veiculo))
                                .andExpect(status().isCreated()));
        long peca = peca(session, 10, 10);
        for (int i = 0; i < 2; i++) {
            enviar(
                            "/ordens/" + ordem + "/itens/peca",
                            session,
                            "{\"pecaId\":" + peca + ",\"quantidade\":2}")
                    .andExpect(status().isNoContent());
        }
        EmpresaContext.definir(891L);
        try {
            assertThat(pecasService.pecasMaisUsadas()).isEmpty();
        } finally {
            EmpresaContext.limpar();
        }
        enviar("/ordens/" + ordem + "/aprovar", session, "{}").andExpect(status().isNoContent());
        mvc.perform(get("/api/pecas").session(session)).andExpect(jsonPath("$[0].saldo").value(6));
        EmpresaContext.definir(892L);
        try {
            assertThat(pecasService.pecasMaisUsadas()).isEmpty();
        } finally {
            EmpresaContext.limpar();
        }
        mvc.perform(
                        put("/api/ordens/" + ordem)
                                .session(session)
                                .header("X-CSRF-Token", csrf.obterOuCriar(session))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"status\":\"ORCAMENTO\",\"descricao\":\"Teste\",\"quilometragemAtual\":0}"))
                .andExpect(status().isBadRequest());
        var restrito = new MockHttpSession();
        SessaoAutenticacao.iniciar(
                restrito,
                new UsuarioAutenticado(
                        899L,
                        891L,
                        "Empresa teste",
                        "Estoquista",
                        Perfil.BALCONISTA,
                        Set.of(br.edu.fag.sputnik.domain.PermissaoModulo.PECAS),
                        false));
        mvc.perform(get("/api/dashboard").session(restrito))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordensAbertas").value(0))
                .andExpect(jsonPath("$.porStatus").isEmpty())
                .andExpect(jsonPath("$.aguardandoPeca").isEmpty())
                .andExpect(jsonPath("$.retornos").isEmpty());
    }
}
