package br.edu.fag.sputnik.repository;

import br.edu.fag.sputnik.domain.OrdemServico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    List<OrdemServico> findAllByOrderByAberturaDesc();
}
