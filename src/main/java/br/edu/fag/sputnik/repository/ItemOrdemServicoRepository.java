package br.edu.fag.sputnik.repository;

import br.edu.fag.sputnik.domain.ItemOrdemServico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemOrdemServicoRepository extends JpaRepository<ItemOrdemServico, Long> {
    List<ItemOrdemServico> findByOrdemServicoEmpresaId(Long empresaId);
    List<ItemOrdemServico> findByOrdemServicoIdOrderByIdDesc(Long ordemServicoId);
}
