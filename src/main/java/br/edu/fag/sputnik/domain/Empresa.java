package br.edu.fag.sputnik.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

/** Organização cliente do SaaS. Toda conta de usuário pertence a uma empresa. */
@Entity
@Table(name = "empresas", uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 60)
    private String slug;

    @Column(length = 30)
    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanoAssinatura plano = PlanoAssinatura.STARTER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAssinatura statusAssinatura = StatusAssinatura.EM_TESTE;

    private LocalDate periodoTesteAte;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public PlanoAssinatura getPlano() {
        return plano;
    }

    public void setPlano(PlanoAssinatura plano) {
        this.plano = plano;
    }

    public StatusAssinatura getStatusAssinatura() {
        return statusAssinatura;
    }

    public void setStatusAssinatura(StatusAssinatura statusAssinatura) {
        this.statusAssinatura = statusAssinatura;
    }

    public LocalDate getPeriodoTesteAte() {
        return periodoTesteAte;
    }

    public void setPeriodoTesteAte(LocalDate periodoTesteAte) {
        this.periodoTesteAte = periodoTesteAte;
    }
}
