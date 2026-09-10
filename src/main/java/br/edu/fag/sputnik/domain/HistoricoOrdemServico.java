package br.edu.fag.sputnik.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Registro de auditoria das alterações feitas na ordem de serviço. */
@Entity
public class HistoricoOrdemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private OrdemServico ordemServico;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false)
    private String responsavel;

    @Column(nullable = false, length = 500)
    private String descricao;

    public Long getId() {
        return id;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
