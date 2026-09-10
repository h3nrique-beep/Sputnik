package br.edu.fag.sputnik.repository;

import br.edu.fag.sputnik.domain.UsuarioSistema;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, Long> {
    Optional<UsuarioSistema> findByUsuarioIgnoreCase(String usuario);

    Optional<UsuarioSistema> findByEmailIgnoreCase(String email);

    boolean existsByUsuarioIgnoreCase(String usuario);

    boolean existsByEmailIgnoreCase(String email);
}
