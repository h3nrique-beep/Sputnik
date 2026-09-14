package br.edu.fag.sputnik.auth;

import br.edu.fag.sputnik.domain.Perfil;
import br.edu.fag.sputnik.domain.PermissaoModulo;

import java.io.Serializable;
import java.util.Set;

/** Dados mínimos do usuário mantidos na sessão após o login. */
public record UsuarioAutenticado(
        Long usuarioId,
        Long empresaId,
        String empresaNome,
        String nomeExibicao,
        Perfil perfil,
        Set<PermissaoModulo> permissoes,
        boolean trocaSenhaObrigatoria)
        implements Serializable {
    public UsuarioAutenticado(
            Long usuarioId,
            String nomeExibicao,
            Perfil perfil,
            Set<PermissaoModulo> permissoes,
            boolean trocaSenhaObrigatoria) {
        this(usuarioId, null, null, nomeExibicao, perfil, permissoes, trocaSenhaObrigatoria);
    }
}
