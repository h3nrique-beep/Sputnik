package br.edu.fag.sputnik.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios_sistema", uniqueConstraints = @UniqueConstraint(columnNames = "usuario"))
public class UsuarioSistema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String usuario;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String senhaHash;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean trocaSenhaObrigatoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_permissoes", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permissao", nullable = false)
    private Set<PermissaoModulo> permissoes = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public boolean isTrocaSenhaObrigatoria() {
        return trocaSenhaObrigatoria;
    }

    public void setTrocaSenhaObrigatoria(boolean trocaSenhaObrigatoria) {
        this.trocaSenhaObrigatoria = trocaSenhaObrigatoria;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Set<PermissaoModulo> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(Set<PermissaoModulo> permissoes) {
        this.permissoes = permissoes;
    }
}
