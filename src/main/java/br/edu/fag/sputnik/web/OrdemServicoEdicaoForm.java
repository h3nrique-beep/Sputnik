package br.edu.fag.sputnik.web;

import br.edu.fag.sputnik.domain.StatusOrdemServico;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class OrdemServicoEdicaoForm {
    @Valid private br.edu.fag.sputnik.domain.DadosDocumentoOs documento;

    public br.edu.fag.sputnik.domain.DadosDocumentoOs getDocumento() {
        return documento;
    }

    public void setDocumento(br.edu.fag.sputnik.domain.DadosDocumentoOs documento) {
        this.documento = documento;
    }

    @NotBlank
    @Size(max = 1200)
    private String descricao;

    @NotNull @PositiveOrZero private Integer quilometragemAtual;
    @NotNull private StatusOrdemServico status;

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

    public StatusOrdemServico getStatus() {
        return status;
    }

    public void setStatus(StatusOrdemServico status) {
        this.status = status;
    }
}
