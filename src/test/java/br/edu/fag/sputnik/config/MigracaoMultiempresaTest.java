package br.edu.fag.sputnik.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.fag.sputnik.domain.Empresa;
import br.edu.fag.sputnik.domain.Peca;
import br.edu.fag.sputnik.repository.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import jakarta.persistence.EntityManager;

@DataJpaTest
class MigracaoMultiempresaTest {
    @Autowired EmpresaRepository empresas;
    @Autowired PecaRepository pecas;
    @Autowired ClienteRepository clientes;
    @Autowired VeiculoRepository veiculos;
    @Autowired OrdemServicoRepository ordens;
    @Autowired MovimentacaoRepository movimentacoes;
    @Autowired LancamentoFinanceiroRepository lancamentos;
    @Autowired EntityManager entityManager;

    @Test
    void atribuiEmpresaPadraoARegistrosLegados() {
        Empresa empresa = new Empresa();
        empresa.setNome("Oficina teste");
        empresa.setSlug("oficina-teste");
        empresa = empresas.saveAndFlush(empresa);
        Peca peca = new Peca();
        peca.setCodigo("LEGADA");
        peca.setNome("Peça legada");
        peca.setFabricante("Teste");
        peca.setCategoria("Teste");
        peca.setFornecedor("Teste");
        peca.setLocalizacao("A1");
        peca.setPreco(BigDecimal.ONE);
        peca.setSaldo(1);
        peca.setEstoqueDisponivel(1);
        pecas.saveAndFlush(peca);

        new MigracaoMultiempresa().migrar(
                empresas, clientes, pecas, veiculos, ordens, movimentacoes, lancamentos);
        entityManager.flush();
        entityManager.clear();

        assertThat(pecas.findById(peca.getId()).orElseThrow().getEmpresaId()).isEqualTo(empresa.getId());
    }
}
