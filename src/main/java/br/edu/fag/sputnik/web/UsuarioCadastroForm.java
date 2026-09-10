package br.edu.fag.sputnik.web;

import br.edu.fag.sputnik.domain.PermissaoModulo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

public class UsuarioCadastroForm {
    @NotBlank private String nome;
    @Email private String email;
    @NotBlank private String senha;
    private Set<PermissaoModulo> permissoes = new HashSet<>();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Set<PermissaoModulo> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(Set<PermissaoModulo> permissoes) {
        this.permissoes = permissoes;
    }
}
