package br.edu.fag.sputnik.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Item de peça ou serviço lançado em uma ordem de serviço. */
@Entity
public class ItemOrdemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private OrdemServico ordemServico;

    @ManyToOne private Peca peca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoItemOrdemServico tipo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    private boolean estoqueBaixado;

    public Long getId() {
        return id;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }

    public Peca getPeca() {
        return peca;
    }

    public void setPeca(Peca peca) {
        this.peca = peca;
    }

    public TipoItemOrdemServico getTipo() {
        return tipo;
    }

    public void setTipo(TipoItemOrdemServico tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public boolean isEstoqueBaixado() {
        return estoqueBaixado;
    }

    public void setEstoqueBaixado(boolean estoqueBaixado) {
        this.estoqueBaixado = estoqueBaixado;
    }

    public BigDecimal getTotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
