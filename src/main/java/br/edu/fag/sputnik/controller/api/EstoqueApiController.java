package br.edu.fag.sputnik.controller.api;

import static br.edu.fag.sputnik.controller.api.mapper.ApiMapper.pecaDto;

import br.edu.fag.sputnik.controller.api.dto.ReposicaoDto;
import br.edu.fag.sputnik.controller.api.mapper.ApiMapper;
import br.edu.fag.sputnik.domain.Peca;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.EstoqueService;
import br.edu.fag.sputnik.web.MovimentacaoForm;

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

import java.util.Map;

@RestController
@RequestMapping("/api")
public class EstoqueApiController extends ApiSupport {
    private final EstoqueService estoque;

    public EstoqueApiController(AutorizacaoService autorizacao, EstoqueService estoque) {
        super(autorizacao);
        this.estoque = estoque;
    }

    @GetMapping("/pecas")
    public ResponseEntity<?> pecas(
            @RequestParam(required = false) String busca, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.PECAS)) return proibido();
        return ResponseEntity.ok(estoque.listar(busca).stream().map(ApiMapper::pecaDto).toList());
    }

    @PostMapping("/pecas")
    public ResponseEntity<?> salvarPeca(@Valid @RequestBody Peca peca, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.PECAS)) return proibido();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(pecaDto(estoque.salvar(peca)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }

    @PostMapping("/movimentacoes")
    public ResponseEntity<?> movimentar(
            @Valid @RequestBody MovimentacaoForm form, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.PECAS)) return proibido();
        try {
            estoque.movimentar(form, operador(session));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }

    @GetMapping("/relatorio-mensal")
    public ResponseEntity<?> relatorioMensal(HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.RELATORIO_MENSAL)) return proibido();
        return ResponseEntity.ok(
                estoque.relatorioMensal().stream().map(ApiMapper::movimentacaoDto).toList());
    }

    @GetMapping("/reposicao")
    public ResponseEntity<?> reposicao(HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.REPOSICAO)) return proibido();
        var pecas =
                estoque.criticas().stream()
                        .map(peca -> new ReposicaoDto(pecaDto(peca), estoque.sugestao(peca)))
                        .toList();
        return ResponseEntity.ok(
                Map.of(
                        "pecas",
                        pecas,
                        "custo",
                        estoque.custoReposicao(),
                        "zeradas",
                        estoque.quantidadeZeradas()));
    }
}
