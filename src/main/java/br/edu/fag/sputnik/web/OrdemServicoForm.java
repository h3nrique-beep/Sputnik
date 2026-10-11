package br.edu.fag.sputnik.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrdemServicoForm {
    @Valid private br.edu.fag.sputnik.domain.DadosDocumentoOs documento;

    public br.edu.fag.sputnik.domain.DadosDocumentoOs getDocumento() {
        return documento;
    }

    public void setDocumento(br.edu.fag.sputnik.domain.DadosDocumentoOs documento) {
        this.documento = documento;
    }

    @NotNull private Long clienteId;
    @NotNull private Long veiculoId;

    @NotBlank
    @Size(max = 1200)
    private String descricao;

    @NotNull @PositiveOrZero private Integer quilometragemAtual;
    private boolean orcamento;
    private LocalDateTime agendamento;
    private LocalDate retornoEm;
    private LocalDate garantiaAte;

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

    public boolean isOrcamento() {
        return orcamento;
    }

    public void setOrcamento(boolean orcamento) {
        this.orcamento = orcamento;
    }

    public LocalDateTime getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(LocalDateTime agendamento) {
        this.agendamento = agendamento;
    }

    public LocalDate getRetornoEm() {
        return retornoEm;
    }

    public void setRetornoEm(LocalDate retornoEm) {
        this.retornoEm = retornoEm;
    }

    public LocalDate getGarantiaAte() {
        return garantiaAte;
    }

    public void setGarantiaAte(LocalDate garantiaAte) {
        this.garantiaAte = garantiaAte;
    }
}
