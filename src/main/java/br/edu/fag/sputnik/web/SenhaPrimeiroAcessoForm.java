package br.edu.fag.sputnik.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SenhaPrimeiroAcessoForm {
    @NotBlank
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message =
                    "A senha deve conter letra maiúscula, minúscula, número e caractere especial.")
    private String senha;

    @NotBlank private String confirmacaoSenha;

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmacaoSenha() {
        return confirmacaoSenha;
    }

    public void setConfirmacaoSenha(String confirmacaoSenha) {
        this.confirmacaoSenha = confirmacaoSenha;
    }
}
