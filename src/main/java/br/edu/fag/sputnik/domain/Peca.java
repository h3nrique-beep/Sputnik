package br.edu.fag.sputnik.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pecas", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
public class Peca extends EntidadeDaEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String codigo;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @NotBlank private String fabricante;
    @NotBlank private String categoria;
    @NotBlank private String fornecedor;
    @NotBlank private String localizacao;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal preco;

    @NotNull
    @Min(0)
    private Integer saldo = 0;

    /** Estoque fisicamente disponível para novas ordens de serviço. */
    @Min(0)
    private Integer estoqueDisponivel = 0;

    @NotNull
    @Min(0)
    private Integer estoqueMinimo = 0;

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getSaldo() {
        return saldo;
    }

    public void setSaldo(Integer saldo) {
        this.saldo = saldo;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public Integer getEstoqueDisponivel() {
        return estoqueDisponivel == null ? saldo : estoqueDisponivel;
    }

    public void setEstoqueDisponivel(Integer estoqueDisponivel) {
        this.estoqueDisponivel = estoqueDisponivel;
    }

    public boolean temEstoqueDisponivel() {
        return estoqueDisponivel != null;
    }

    public boolean estaZerada() {
        return getEstoqueDisponivel() == 0;
    }

    public boolean estaAbaixoDoMinimo() {
        return getEstoqueDisponivel() < estoqueMinimo;
    }
}
