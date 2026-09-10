package br.edu.fag.sputnik.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ItemPecaOrdemForm {
    @NotNull private Long pecaId;

    @NotNull
    @Min(1)
    private Integer quantidade;

    public Long getPecaId() {
        return pecaId;
    }

    public void setPecaId(Long pecaId) {
        this.pecaId = pecaId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
