package br.edu.fag.sputnik.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class LancamentoFinanceiro extends EntidadeDaEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLancamentoFinanceiro tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLancamentoFinanceiro status = StatusLancamentoFinanceiro.PENDENTE;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 160)
    private String descricao;

    @Column(length = 80)
    private String categoria;

    @Column(nullable = false)
    private LocalDate vencimento;

    private LocalDate pagamentoEm;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @ManyToOne private OrdemServico ordemServico;

    public Long getId() {
        return id;
    }

    public TipoLancamentoFinanceiro getTipo() {
        return tipo;
    }

    public void setTipo(TipoLancamentoFinanceiro tipo) {
        this.tipo = tipo;
    }

    public StatusLancamentoFinanceiro getStatus() {
        return status;
    }

    public void setStatus(StatusLancamentoFinanceiro status) {
        this.status = status;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
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

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public LocalDate getPagamentoEm() {
        return pagamentoEm;
    }

    public void setPagamentoEm(LocalDate pagamentoEm) {
        this.pagamentoEm = pagamentoEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }
}
