package br.edu.fag.sputnik.repository;

import br.edu.fag.sputnik.domain.LancamentoFinanceiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LancamentoFinanceiroRepository extends JpaRepository<LancamentoFinanceiro, Long> {
    List<LancamentoFinanceiro> findAllByOrderByVencimentoAscIdDesc();
}
