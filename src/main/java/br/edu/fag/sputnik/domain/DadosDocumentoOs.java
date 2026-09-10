package br.edu.fag.sputnik.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/** Dados complementares preservados na própria OS, sem modificar o cadastro vinculado. */
@Embeddable
public class DadosDocumentoOs {
    @Size(max = 160)
    public String oficinaNome;

    @Size(max = 250)
    public String oficinaEndereco;

    @Size(max = 80)
    public String oficinaContato;

    @Size(max = 30)
    public String oficinaDocumento;

    @Size(max = 120)
    public String condicaoPagamento;

    @Min(0)
    @Max(365)
    public Integer validadeDias;

    public LocalDateTime previsaoEntrega;
    public LocalDateTime previsaoOrcamento;

    @Pattern(regexp = "EXTERNO|INTERNO|GARANTIA|REVISAO")
    public String tipoServico;

    @Size(max = 160)
    public String pagadorNome;

    @Size(max = 30)
    public String pagadorDocumento;

    @Size(max = 80)
    public String pagadorTelefone;

    @Email
    @Size(max = 160)
    public String pagadorEmail;

    @Size(max = 250)
    public String pagadorEndereco;

    @Size(max = 250)
    public String proprietarioEndereco;

    @Email
    @Size(max = 160)
    public String proprietarioEmail;

    @Size(max = 80)
    public String motor;

    @Size(max = 80)
    public String cor;

    @Min(1900)
    @Max(2200)
    public Integer anoModelo;

    @Min(0)
    @Max(100)
    public Integer combustivelPercentual;

    @Size(max = 120)
    public String consultorTecnico;

    @Size(max = 40)
    public String prisma;

    @Size(max = 80)
    public String certificadoGarantia;

    @Size(max = 1200)
    @Column(length = 1200)
    public String observacoes;

    @Size(max = 250)
    public String reembolso;

    public LocalDateTime retiradaEm;
}
