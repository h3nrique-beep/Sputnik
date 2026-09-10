package br.edu.fag.sputnik.controller.api;

import static br.edu.fag.sputnik.controller.api.mapper.ApiMapper.ordemResumo;

import br.edu.fag.sputnik.controller.api.dto.OrdemDetalheDto;
import br.edu.fag.sputnik.controller.api.mapper.ApiMapper;
import br.edu.fag.sputnik.domain.OrdemServico;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.EstoqueService;
import br.edu.fag.sputnik.service.OrdemServicoPdfService;
import br.edu.fag.sputnik.web.ItemPecaOrdemForm;
import br.edu.fag.sputnik.web.OrdemServicoEdicaoForm;
import br.edu.fag.sputnik.web.OrdemServicoForm;
import br.edu.fag.sputnik.web.ServicoOrdemForm;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrdensApiController extends ApiSupport {
    private final EstoqueService estoque;
    private final OrdemServicoPdfService pdfsOrdem;

    public OrdensApiController(
            AutorizacaoService autorizacao,
            EstoqueService estoque,
            OrdemServicoPdfService pdfsOrdem) {
        super(autorizacao);
        this.estoque = estoque;
        this.pdfsOrdem = pdfsOrdem;
    }

    @GetMapping("/ordens")
    public ResponseEntity<?> ordens(HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        return ResponseEntity.ok(estoque.ordens().stream().map(ApiMapper::ordemResumo).toList());
    }

    @PostMapping("/ordens")
    public ResponseEntity<?> abrirOrdem(
            @Valid @RequestBody OrdemServicoForm form, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ordemResumo(estoque.abrirOrdem(form, operador(session))));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }

    @GetMapping("/ordens/{id}")
    public ResponseEntity<?> detalheOrdem(@PathVariable Long id, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        try {
            OrdemServico ordem = estoque.buscarOrdem(id);
            var itens = estoque.itensDaOrdem(id).stream().map(ApiMapper::itemDto).toList();
            var historico =
                    estoque.historicoDaOrdem(id).stream().map(ApiMapper::historicoDto).toList();
            return ResponseEntity.ok(new OrdemDetalheDto(ordemResumo(ordem), itens, historico));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/ordens/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> baixarPdfOrdem(@PathVariable Long id, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        try {
            OrdemServico ordem = estoque.buscarOrdem(id);
            byte[] arquivo =
                    pdfsOrdem.gerar(ordem, estoque.itensDaOrdem(id), estoque.historicoDaOrdem(id));
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=ordem-servico-" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(arquivo);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/ordens/{id}/impressao", produces = "text/html;charset=UTF-8")
    public ResponseEntity<?> imprimirOrdem(@PathVariable Long id, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
                    .body(
                            pdfsOrdem.impressao(
                                    estoque.buscarOrdem(id),
                                    estoque.itensDaOrdem(id),
                                    estoque.historicoDaOrdem(id)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/ordens/{id}")
    public ResponseEntity<?> editarOrdem(
            @PathVariable Long id,
            @Valid @RequestBody OrdemServicoEdicaoForm form,
            HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        try {
            estoque.atualizarOrdem(id, form, operador(session));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }

    @PostMapping("/ordens/{id}/itens/peca")
    public ResponseEntity<?> adicionarPeca(
            @PathVariable Long id,
            @Valid @RequestBody ItemPecaOrdemForm form,
            HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        try {
            estoque.adicionarPecaNaOrdem(
                    id, form.getPecaId(), form.getQuantidade(), operador(session));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }

    @PostMapping("/ordens/{id}/itens/servico")
    public ResponseEntity<?> adicionarServico(
            @PathVariable Long id, @Valid @RequestBody ServicoOrdemForm form, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.ORDENS)) return proibido();
        try {
            estoque.adicionarServicoNaOrdem(
                    id, form.getDescricao(), form.getValor(), operador(session));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", exception.getMessage()));
        }
    }
}
