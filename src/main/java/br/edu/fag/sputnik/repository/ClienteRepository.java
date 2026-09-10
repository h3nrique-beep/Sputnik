package br.edu.fag.sputnik.repository;

import br.edu.fag.sputnik.domain.Cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente>
            findByNomeContainingIgnoreCaseOrTelefoneContainingIgnoreCaseOrCpfContainingIgnoreCase(
                    String nome, String telefone, String cpf);
}
