package br.edu.fag.sputnik.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import br.edu.fag.sputnik.domain.Peca;
import br.edu.fag.sputnik.repository.PecaRepository;
import br.edu.fag.sputnik.repository.MovimentacaoRepository;
import br.edu.fag.sputnik.repository.ItemOrdemServicoRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;

class PecasAuditoriaTest {
    @Test
    void csvEscapaDelimitadoresAspasQuebrasEFormulas() {
        var repository = mock(PecaRepository.class);
        var service = new PecasService(repository, mock(MovimentacaoRepository.class),
                mock(ItemOrdemServicoRepository.class), mock(EntityManager.class));
        var peca = new Peca();
        peca.setCodigo("=1+1");
        peca.setNome("Filtro;\"especial\"\nnovo");
        peca.setFornecedor("@fornecedor");
        peca.setSaldo(10);
        peca.setEstoqueDisponivel(0);
        peca.setEstoqueMinimo(2);
        when(repository.findAll()).thenReturn(List.of(peca));
        assertThat(service.gerarCsvReposicao()).contains(
                "\"'=1+1\";\"Filtro;\"\"especial\"\"\nnovo\";\"'@fornecedor\";10;2;4");
        assertThat(service.sugestao(peca)).isEqualTo(4);
    }
}
