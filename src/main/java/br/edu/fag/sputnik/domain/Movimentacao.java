package br.edu.fag.sputnik.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Movimentacao extends EntidadeDaEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Peca peca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private Integer saldoResultante;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false)
    private String operador;

    private String observacao;

    public Long getId() {
        return id;
    }

    public Peca getPeca() {
        return peca;
    }

    public void setPeca(Peca v) {
        peca = v;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao v) {
        tipo = v;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer v) {
        quantidade = v;
    }

    public Integer getSaldoResultante() {
        return saldoResultante;
    }

    public void setSaldoResultante(Integer v) {
        saldoResultante = v;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime v) {
        data = v;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String v) {
        operador = v;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String v) {
        observacao = v;
    }
}
