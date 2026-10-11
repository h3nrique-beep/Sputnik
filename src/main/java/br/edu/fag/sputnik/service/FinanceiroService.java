package br.edu.fag.sputnik.service;

import br.edu.fag.sputnik.domain.LancamentoFinanceiro;
import br.edu.fag.sputnik.domain.OrdemServico;
import br.edu.fag.sputnik.domain.StatusLancamentoFinanceiro;
import br.edu.fag.sputnik.repository.LancamentoFinanceiroRepository;
import br.edu.fag.sputnik.repository.OrdemServicoRepository;
import br.edu.fag.sputnik.tenant.EmpresaContext;
import br.edu.fag.sputnik.web.LancamentoFinanceiroForm;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FinanceiroService {
    private final LancamentoFinanceiroRepository lancamentos;
    private final OrdemServicoRepository ordens;

    public FinanceiroService(
            LancamentoFinanceiroRepository lancamentos, OrdemServicoRepository ordens) {
        this.lancamentos = lancamentos;
        this.ordens = ordens;
    }

    public List<LancamentoFinanceiro> listar() {
        return lancamentos.findAllByOrderByVencimentoAscIdDesc();
    }

    @Transactional
    public LancamentoFinanceiro criar(LancamentoFinanceiroForm form) {
        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        lancamento.setTipo(form.getTipo());
        lancamento.setDescricao(form.getDescricao().trim());
        lancamento.setCategoria(form.getCategoria() == null ? null : form.getCategoria().trim());
        lancamento.setValor(form.getValor());
        lancamento.setVencimento(form.getVencimento());
        lancamento.setCriadoEm(LocalDateTime.now());
        if (form.getOrdemServicoId() != null) {
            OrdemServico ordem =
                    EmpresaContext.exigirPertencimento(
                            ordens.findById(form.getOrdemServicoId())
                                    .orElseThrow(
                                            () ->
                                                    new IllegalArgumentException(
                                                            "Ordem de serviço não encontrada.")),
                            "Ordem de serviço não encontrada.");
            lancamento.setOrdemServico(ordem);
        }
        return lancamentos.save(lancamento);
    }

    @Transactional
    public void baixar(Long id) {
        LancamentoFinanceiro lancamento =
                EmpresaContext.exigirPertencimento(
                        lancamentos
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                new IllegalArgumentException(
                                                        "Lançamento financeiro não encontrado.")),
                        "Lançamento financeiro não encontrado.");
        if (lancamento.getStatus() == StatusLancamentoFinanceiro.PAGO) {
            throw new IllegalArgumentException("Este lançamento já foi baixado.");
        }
        lancamento.setStatus(StatusLancamentoFinanceiro.PAGO);
        lancamento.setPagamentoEm(LocalDate.now());
        lancamentos.save(lancamento);
    }
}
