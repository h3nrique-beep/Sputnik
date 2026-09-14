package br.edu.fag.sputnik.web;

import jakarta.validation.constraints.*;

public class CadastroEmpresaForm {
    @NotBlank @Size(max = 120) private String empresaNome;
    @NotBlank @Pattern(regexp = "[a-z0-9-]{3,60}") private String slug;
    @NotBlank @Size(max = 120) private String nomeAdministrador;
    @NotBlank @Email @Size(max = 160) private String email;
    @NotBlank @Size(min = 12, max = 128) private String senha;
    public String getEmpresaNome() { return empresaNome; }
    public void setEmpresaNome(String empresaNome) { this.empresaNome = empresaNome; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getNomeAdministrador() { return nomeAdministrador; }
    public void setNomeAdministrador(String nomeAdministrador) { this.nomeAdministrador = nomeAdministrador; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
