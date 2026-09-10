package br.edu.fag.sputnik.controller;

import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.EstoqueService;
import br.edu.fag.sputnik.service.ModeloPaginaService;
import br.edu.fag.sputnik.web.MovimentacaoForm;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/** Controla movimentações de estoque e relatórios operacionais. */
@Controller
public class EstoqueController {
    private final EstoqueService estoque;
    private final AutorizacaoService autorizacao;
    private final ModeloPaginaService pagina;

    public EstoqueController(
            EstoqueService estoque, AutorizacaoService autorizacao, ModeloPaginaService pagina) {
        this.estoque = estoque;
        this.autorizacao = autorizacao;
        this.pagina = pagina;
    }

    @GetMapping("/movimentacoes/nova")
    public String novaMovimentacao(Model model, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.PECAS)) return semAcesso(session);
        prepararMovimentacao(model, session);
        model.addAttribute("form", new MovimentacaoForm());
        return "movimentacao-form";
    }

    @PostMapping("/movimentacoes")
    public String movimentar(
            @Valid @ModelAttribute("form") MovimentacaoForm formulario,
            BindingResult resultado,
            Model model,
            HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.PECAS)) return semAcesso(session);
        if (!resultado.hasErrors()) {
            try {
                String operador = autorizacao.usuarioAtual(session).orElseThrow().nomeExibicao();
                estoque.movimentar(formulario, operador);
                return "redirect:/movimentacoes/nova?sucesso=movimentacao";
            } catch (IllegalArgumentException exception) {
                resultado.reject("movimentacao", exception.getMessage());
            }
        }
        prepararMovimentacao(model, session);
        return "movimentacao-form";
    }

    @GetMapping({"/relatorio-mensal", "/historico"})
    public String relatorioMensal(Model model, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.RELATORIO_MENSAL)) return semAcesso(session);
        pagina.preparar(model, session, "mensal");
        model.addAttribute("movimentos", estoque.relatorioMensal());
        return "historico";
    }

    @GetMapping("/reposicao")
    public String reposicao(Model model, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.REPOSICAO)) return semAcesso(session);
        pagina.preparar(model, session, "reposicao");
        BigDecimal custo = estoque.custoReposicao();
        model.addAttribute("custo", custo);
        model.addAttribute("zeradas", estoque.quantidadeZeradas());
        model.addAttribute("sugestao", estoque);
        return "reposicao";
    }

    @GetMapping(value = "/reposicao.csv", produces = "text/csv")
    @ResponseBody
    public ResponseEntity<byte[]> exportarReposicao(HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.REPOSICAO)) {
            return ResponseEntity.status(autorizacao.estaLogado(session) ? 403 : 401).build();
        }
        byte[] arquivo = ("\uFEFF" + estoque.gerarCsvReposicao()).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=relatorio-reposicao.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(arquivo);
    }

    private void prepararMovimentacao(Model model, HttpSession session) {
        pagina.preparar(model, session, "movimentacoes");
        model.addAttribute("pecas", estoque.listar(""));
    }

    private boolean temAcesso(HttpSession session, PermissaoModulo modulo) {
        return autorizacao.temAcesso(session, modulo);
    }

    private String semAcesso(HttpSession session) {
        return autorizacao.redirecionarSemAcesso(session);
    }
}
