package br.edu.fag.sputnik.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.edu.fag.sputnik.auth.*;
import br.edu.fag.sputnik.domain.*;
import br.edu.fag.sputnik.web.*;

import jakarta.persistence.EntityManager;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:os_documento_test;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class OrdemDocumentoIntegrationTest {
    @Autowired OrdensService ordensService;
    @Autowired EntityManager em;
    @Autowired MockMvc mvc;

    @Test
    void persisteEditaEProtegeDocumento() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente teste");
        cliente.setCpf("00000000000");
        cliente.setTelefone("4500000000");
        em.persist(cliente);
        Veiculo veiculo = new Veiculo();
        veiculo.setMarcaModelo("Modelo teste");
        veiculo.setPlaca("TST1A23");
        veiculo.setChassi("CHASSI-TESTE-OS");
        veiculo.setAno(2020);
        veiculo.setCliente(cliente);
        em.persist(veiculo);
        em.flush();
        OrdemServicoForm f = new OrdemServicoForm();
        f.setClienteId(cliente.getId());
        f.setVeiculoId(veiculo.getId());
        f.setDescricao("Revisão");
        f.setQuilometragemAtual(100);
        DadosDocumentoOs d = new DadosDocumentoOs();
        d.pagadorNome = "Empresa pagadora";
        d.combustivelPercentual = 25;
        f.setDocumento(d);
        Long id = ordensService.abrirOrdem(f, "Teste").getId();
        em.flush();
        em.clear();
        assertThat(ordensService.buscarOrdem(id).getDocumento().pagadorNome)
                .isEqualTo("Empresa pagadora");
        OrdemServicoEdicaoForm edit = new OrdemServicoEdicaoForm();
        edit.setDescricao("Revisão editada");
        edit.setQuilometragemAtual(101);
        edit.setStatus(StatusOrdemServico.EM_ANDAMENTO);
        d.combustivelPercentual = 50;
        edit.setDocumento(d);
        ordensService.atualizarOrdem(id, edit, "Teste");
        em.flush();
        em.clear();
        assertThat(ordensService.buscarOrdem(id).getDocumento().combustivelPercentual)
                .isEqualTo(50);
        MockHttpSession session = new MockHttpSession();
        SessaoAutenticacao.iniciar(
                session, new UsuarioAutenticado(1L, "Teste", Perfil.ADMIN, Set.of(), false));
        mvc.perform(get("/api/ordens/" + id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordem.documento.pagadorNome").value("Empresa pagadora"));
        mvc.perform(get("/api/ordens/" + id + "/pdf").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
        mvc.perform(get("/api/ordens/" + id + "/impressao").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Empresa pagadora")));
        MockHttpSession restricted = new MockHttpSession();
        SessaoAutenticacao.iniciar(
                restricted,
                new UsuarioAutenticado(2L, "Sem acesso", Perfil.BALCONISTA, Set.of(), false));
        mvc.perform(get("/api/ordens/" + id + "/pdf").session(restricted))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/ordens/" + id + "/impressao").session(restricted))
                .andExpect(status().isForbidden());
    }
}
