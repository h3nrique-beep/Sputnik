package br.edu.fag.sputnik.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class OrdemServicoForm {
    @jakarta.validation.Valid private br.edu.fag.sputnik.domain.DadosDocumentoOs documento;

    public br.edu.fag.sputnik.domain.DadosDocumentoOs getDocumento() {
        return documento;
    }

    public void setDocumento(br.edu.fag.sputnik.domain.DadosDocumentoOs documento) {
        this.documento = documento;
    }

    @NotNull private Long clienteId;
    @NotNull private Long veiculoId;

    @NotBlank
    @jakarta.validation.constraints.Size(max = 1200)
    private String descricao;

    @NotNull @PositiveOrZero private Integer quilometragemAtual;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(Long veiculoId) {
        this.veiculoId = veiculoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getQuilometragemAtual() {
        return quilometragemAtual;
    }

    public void setQuilometragemAtual(Integer quilometragemAtual) {
        this.quilometragemAtual = quilometragemAtual;
    }
}
