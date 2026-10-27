package br.edu.fag.sputnik.controller.api;

import java.util.List;
import br.edu.fag.sputnik.controller.api.dto.LancamentoDto;
import br.edu.fag.sputnik.domain.LancamentoFinanceiro;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.FinanceiroService;
import br.edu.fag.sputnik.web.LancamentoFinanceiroForm;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/financeiro")
public class FinanceiroApiController extends ApiSupport {
    private final FinanceiroService financeiro;

    public FinanceiroApiController(AutorizacaoService autorizacao, FinanceiroService financeiro) {
        super(autorizacao);
        this.financeiro = financeiro;
    }

    @GetMapping
    public ResponseEntity<List<LancamentoDto>> listar(HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.FINANCEIRO)) return proibido();
        return ResponseEntity.ok(financeiro.listar().stream().map(this::dto).toList());
    }

    @PostMapping
    public ResponseEntity<LancamentoDto> criar(
            @Valid @RequestBody LancamentoFinanceiroForm form, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.FINANCEIRO)) return proibido();
        return ResponseEntity.status(HttpStatus.CREATED).body(dto(financeiro.criar(form)));
    }

    @PostMapping("/{id}/baixar")
    public ResponseEntity<Void> baixar(@PathVariable Long id, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.FINANCEIRO)) return proibido();
        financeiro.baixar(id);
        return ResponseEntity.noContent().build();
    }

    private LancamentoDto dto(LancamentoFinanceiro l) {
        return new LancamentoDto(
                l.getId(),
                l.getTipo().name(),
                l.getStatus().name(),
                l.getDescricao(),
                l.getCategoria(),
                l.getValor(),
                l.getVencimento(),
                l.getPagamentoEm(),
                l.getOrdemServico() == null ? null : l.getOrdemServico().getId());
    }
}
