package br.edu.fag.sputnik.web;

import br.edu.fag.sputnik.domain.TipoLancamentoFinanceiro;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LancamentoFinanceiroForm {
    @NotNull private TipoLancamentoFinanceiro tipo;

    @NotBlank
    @Size(max = 160)
    private String descricao;

    @Size(max = 80)
    private String categoria;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal valor;

    @NotNull private LocalDate vencimento;
    private Long ordemServicoId;

    public TipoLancamentoFinanceiro getTipo() {
        return tipo;
    }

    public void setTipo(TipoLancamentoFinanceiro tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public Long getOrdemServicoId() {
        return ordemServicoId;
    }

    public void setOrdemServicoId(Long ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }
}
