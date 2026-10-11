package br.edu.fag.sputnik.web;

import br.edu.fag.sputnik.domain.TipoMovimentacao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados validados para uma entrada ou saída manual de estoque. */
public class MovimentacaoForm {
    @NotNull private Long pecaId;

    @NotNull private TipoMovimentacao tipo;

    @NotNull
    @Min(1)
    private Integer quantidade;

    @Size(max = 255, message = "A observação deve ter até 255 caracteres.")
    private String observacao;

    public Long getPecaId() {
        return pecaId;
    }

    public void setPecaId(Long pecaId) {
        this.pecaId = pecaId;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
