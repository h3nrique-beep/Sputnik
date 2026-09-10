package br.edu.fag.sputnik.web;

import br.edu.fag.sputnik.domain.PermissaoModulo;

import java.util.HashSet;
import java.util.Set;

public class UsuarioPermissoesForm {
    private Set<PermissaoModulo> permissoes = new HashSet<>();

    public Set<PermissaoModulo> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(Set<PermissaoModulo> permissoes) {
        this.permissoes = permissoes;
    }
}
