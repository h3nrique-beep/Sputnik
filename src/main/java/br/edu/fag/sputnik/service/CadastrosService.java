package br.edu.fag.sputnik.service;

import br.edu.fag.sputnik.domain.Cliente;
import br.edu.fag.sputnik.domain.Veiculo;
import br.edu.fag.sputnik.repository.ClienteRepository;
import br.edu.fag.sputnik.repository.VeiculoRepository;
import br.edu.fag.sputnik.tenant.EmpresaContext;

import org.springframework.stereotype.Service;

import java.util.List;

/** Regras de cadastros de clientes e veículos. */
@Service
public class CadastrosService {
    private final ClienteRepository clientes;
    private final VeiculoRepository veiculos;

    public CadastrosService(ClienteRepository clientes, VeiculoRepository veiculos) {
        this.clientes = clientes;
        this.veiculos = veiculos;
    }

    public List<Cliente> clientes() {
        return clientes.findAll();
    }

    public List<Cliente> clientes(String busca) {
        if (busca == null || busca.isBlank()) {
            return clientes();
        }
        return clientes
                .findByNomeContainingIgnoreCaseOrTelefoneContainingIgnoreCaseOrCpfContainingIgnoreCase(
                        busca, busca, busca);
    }

    public Cliente salvarCliente(Cliente cliente) {
        return clientes.save(cliente);
    }

    public List<Veiculo> veiculos() {
        return veiculos.findAll();
    }

    public List<Veiculo> veiculos(String busca) {
        if (busca == null || busca.isBlank()) {
            return veiculos();
        }
        return veiculos.findByPlacaContainingIgnoreCaseOrChassiContainingIgnoreCase(busca, busca);
    }

    public List<Veiculo> veiculosVinculados() {
        return veiculos.findByClienteIsNotNull();
    }

    public Veiculo salvarVeiculo(Veiculo veiculo, Long clienteId) {
        veiculo.setCliente(clienteId == null ? null : buscarCliente(clienteId));
        veiculo.setPlaca(veiculo.getPlaca().trim().toUpperCase());
        if (veiculo.getChassi() != null && !veiculo.getChassi().isBlank()) {
            veiculo.setChassi(veiculo.getChassi().trim().toUpperCase());
        }
        return veiculos.save(veiculo);
    }

    private Cliente buscarCliente(Long id) {
        return EmpresaContext.exigirPertencimento(
                clientes.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Pessoa não encontrada.")),
                "Pessoa não encontrada.");
    }
}
