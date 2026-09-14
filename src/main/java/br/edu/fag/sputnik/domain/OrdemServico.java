package br.edu.fag.sputnik.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class OrdemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @ManyToOne(optional = false)
    private Veiculo veiculo;

    @Column(nullable = false)
    private LocalDateTime abertura;

    @Column(nullable = false)
    private String status;

    @Column(length = 1200)
    private String descricao;

    private Integer quilometragemAbertura;

    private LocalDateTime ultimaAtualizacao;

    private LocalDateTime agendamento;
    private LocalDate retornoEm;
    private LocalDate garantiaAte;

    @Embedded private DadosDocumentoOs documento;

    public DadosDocumentoOs getDocumento() {
        return documento;
    }

    public void setDocumento(DadosDocumentoOs documento) {
        this.documento = documento;
    }

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<ItemOrdemServico> itens = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("data DESC")
    private List<HistoricoOrdemServico> historico = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public LocalDateTime getAbertura() {
        return abertura;
    }

    public void setAbertura(LocalDateTime abertura) {
        this.abertura = abertura;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getQuilometragemAbertura() {
        return quilometragemAbertura;
    }

    public void setQuilometragemAbertura(Integer quilometragemAbertura) {
        this.quilometragemAbertura = quilometragemAbertura;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public LocalDateTime getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(LocalDateTime agendamento) {
        this.agendamento = agendamento;
    }

    public LocalDate getRetornoEm() { return retornoEm; }
    public void setRetornoEm(LocalDate retornoEm) { this.retornoEm = retornoEm; }
    public LocalDate getGarantiaAte() { return garantiaAte; }
    public void setGarantiaAte(LocalDate garantiaAte) { this.garantiaAte = garantiaAte; }

    public List<ItemOrdemServico> getItens() {
        return itens;
    }

    public List<HistoricoOrdemServico> getHistorico() {
        return historico;
    }

    public BigDecimal getTotalPecas() {
        return itens.stream()
                .filter(i -> i.getTipo() == TipoItemOrdemServico.PECA)
                .map(ItemOrdemServico::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalServicos() {
        return itens.stream()
                .filter(i -> i.getTipo() == TipoItemOrdemServico.SERVICO)
                .map(ItemOrdemServico::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalGeral() {
        return getTotalPecas().add(getTotalServicos());
    }
}
