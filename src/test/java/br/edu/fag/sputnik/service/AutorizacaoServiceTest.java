package br.edu.fag.sputnik.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioAutenticado;
import br.edu.fag.sputnik.domain.Perfil;
import br.edu.fag.sputnik.domain.PermissaoModulo;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.EnumSet;

class AutorizacaoServiceTest {
    private final AutorizacaoService autorizacao = new AutorizacaoService();

    @Test
    void administradorTemAcessoATodosOsModulos() {
        MockHttpSession session =
                sessaoDe("Administrador", Perfil.ADMIN, EnumSet.noneOf(PermissaoModulo.class));

        assertThat(autorizacao.ehAdministrador(session)).isTrue();
        assertThat(autorizacao.temAcesso(session, PermissaoModulo.PECAS)).isTrue();
        assertThat(autorizacao.temAcesso(session, PermissaoModulo.REPOSICAO)).isTrue();
    }

    @Test
    void balconistaAcessaSomenteModulosLiberados() {
        MockHttpSession session =
                sessaoDe("Balconista", Perfil.BALCONISTA, EnumSet.of(PermissaoModulo.PECAS));

        assertThat(autorizacao.temAcesso(session, PermissaoModulo.PECAS)).isTrue();
        assertThat(autorizacao.temAcesso(session, PermissaoModulo.ORDENS)).isFalse();
        assertThat(autorizacao.redirecionarSemAcesso(session))
                .isEqualTo("redirect:/inicio?acessoNegado");
    }

    private MockHttpSession sessaoDe(
            String nome, Perfil perfil, EnumSet<PermissaoModulo> permissoes) {
        MockHttpSession session = new MockHttpSession();
        SessaoAutenticacao.iniciar(
                session, new UsuarioAutenticado(1L, nome, perfil, permissoes, false));
        return session;
    }
}
