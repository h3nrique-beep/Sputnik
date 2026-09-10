package br.edu.fag.sputnik.repository;

import br.edu.fag.sputnik.domain.Veiculo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    List<Veiculo> findByClienteId(Long clienteId);

    List<Veiculo> findByClienteIsNotNull();

    List<Veiculo> findByPlacaContainingIgnoreCaseOrChassiContainingIgnoreCase(
            String placa, String chassi);
}
