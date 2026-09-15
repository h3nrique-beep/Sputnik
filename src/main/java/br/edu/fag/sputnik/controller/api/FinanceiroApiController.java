package br.edu.fag.sputnik.controller.api;

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
    public ResponseEntity<?> listar(HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.FINANCEIRO)) return proibido();
        return ResponseEntity.ok(financeiro.listar().stream().map(this::dto).toList());
    }
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody LancamentoFinanceiroForm form, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.FINANCEIRO)) return proibido();
        try { return ResponseEntity.status(HttpStatus.CREATED).body(dto(financeiro.criar(form))); }
        catch (IllegalArgumentException exception) { return invalido(exception); }
    }
    @PostMapping("/{id}/baixar")
    public ResponseEntity<?> baixar(@PathVariable Long id, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.FINANCEIRO)) return proibido();
        try { financeiro.baixar(id); return ResponseEntity.noContent().build(); }
        catch (IllegalArgumentException exception) { return invalido(exception); }
    }
    private Map<String, Object> dto(LancamentoFinanceiro l) {
        return Map.ofEntries(
                Map.entry("id", l.getId()), Map.entry("tipo", l.getTipo().name()),
                Map.entry("status", l.getStatus().name()), Map.entry("descricao", l.getDescricao()),
                Map.entry("categoria", l.getCategoria() == null ? "" : l.getCategoria()),
                Map.entry("valor", l.getValor()), Map.entry("vencimento", l.getVencimento()),
                Map.entry("pagamentoEm", l.getPagamentoEm() == null ? "" : l.getPagamentoEm().toString()),
                Map.entry("ordemServicoId", l.getOrdemServico() == null ? "" : l.getOrdemServico().getId()));
    }
}
