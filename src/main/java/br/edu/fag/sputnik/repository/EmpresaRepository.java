package br.edu.fag.sputnik.repository;

import br.edu.fag.sputnik.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findBySlug(String slug);
}
