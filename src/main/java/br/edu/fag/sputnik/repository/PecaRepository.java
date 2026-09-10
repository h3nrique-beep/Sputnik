package br.edu.fag.sputnik.repository;

import br.edu.fag.sputnik.domain.Peca;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PecaRepository extends JpaRepository<Peca, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);

    List<Peca>
            findByNomeContainingIgnoreCaseOrCodigoContainingIgnoreCaseOrFabricanteContainingIgnoreCase(
                    String nome, String codigo, String fabricante);
}
