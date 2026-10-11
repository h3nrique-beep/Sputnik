package br.edu.fag.sputnik.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.fag.sputnik.repository.EmpresaRepository;
import br.edu.fag.sputnik.repository.UsuarioSistemaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

class UsuarioServiceAutenticacaoTest {
    @Test
    void loginInexistenteTambemExecutaVerificacaoDeSenha() {
        var usuarios = mock(UsuarioSistemaRepository.class);
        var encoder = mock(PasswordEncoder.class);
        when(usuarios.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(usuarios.findByUsuarioIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(encoder.encode(anyString())).thenReturn("hash-de-referencia");
        var service = new UsuarioService(usuarios, mock(EmpresaRepository.class), encoder);

        assertThat(service.autenticar("ninguem@example.test", "qualquer")).isEmpty();
        assertThat(service.autenticar("ninguem@example.test", "outra")).isEmpty();

        // A verificação roda nas duas tentativas; o hash de referência é gerado uma única vez.
        verify(encoder, times(1)).encode(anyString());
        verify(encoder).matches("qualquer", "hash-de-referencia");
        verify(encoder).matches("outra", "hash-de-referencia");
    }
}
