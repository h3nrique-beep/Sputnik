package br.edu.fag.sputnik.repository;

import br.edu.fag.sputnik.domain.Movimentacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
    List<Movimentacao> findAllByOrderByDataDesc();
}
