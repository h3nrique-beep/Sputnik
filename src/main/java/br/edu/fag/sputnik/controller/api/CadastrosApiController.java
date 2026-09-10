package br.edu.fag.sputnik.controller.api;

import static br.edu.fag.sputnik.controller.api.mapper.ApiMapper.clienteDto;
import static br.edu.fag.sputnik.controller.api.mapper.ApiMapper.veiculoDto;

import br.edu.fag.sputnik.controller.api.dto.VeiculoRequest;
import br.edu.fag.sputnik.controller.api.mapper.ApiMapper;
import br.edu.fag.sputnik.domain.Cliente;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.domain.Veiculo;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.EstoqueService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CadastrosApiController extends ApiSupport {
    private final EstoqueService estoque;

    public CadastrosApiController(AutorizacaoService autorizacao, EstoqueService estoque) {
        super(autorizacao);
        this.estoque = estoque;
    }

    @GetMapping("/clientes")
    public ResponseEntity<?> clientes(
            @RequestParam(required = false) String busca, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        return ResponseEntity.ok(
                estoque.clientes(busca).stream().map(ApiMapper::clienteDto).toList());
    }

    @PostMapping("/clientes")
    public ResponseEntity<?> salvarCliente(
            @Valid @RequestBody Cliente cliente, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteDto(estoque.salvarCliente(cliente)));
    }

    @GetMapping("/veiculos")
    public ResponseEntity<?> veiculos(
            @RequestParam(required = false) String busca, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        return ResponseEntity.ok(
                estoque.veiculos(busca).stream().map(ApiMapper::veiculoDto).toList());
    }

    @PostMapping("/veiculos")
    public ResponseEntity<?> salvarVeiculo(
            @Valid @RequestBody VeiculoRequest request, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(request.placa());
        veiculo.setMarcaModelo(request.marcaModelo());
        veiculo.setAno(request.ano());
        veiculo.setChassi(request.chassi());
        veiculo.setQuilometragemAtual(request.quilometragemAtual());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(veiculoDto(estoque.salvarVeiculo(veiculo, request.clienteId())));
    }
}
