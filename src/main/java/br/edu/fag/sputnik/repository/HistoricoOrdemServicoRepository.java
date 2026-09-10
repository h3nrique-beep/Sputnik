package br.edu.fag.sputnik.repository;

import br.edu.fag.sputnik.domain.HistoricoOrdemServico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoOrdemServicoRepository
        extends JpaRepository<HistoricoOrdemServico, Long> {
    List<HistoricoOrdemServico> findByOrdemServicoIdOrderByDataDesc(Long ordemServicoId);
}
