package br.edu.fag.sputnik.controller;

import br.edu.fag.sputnik.domain.Peca;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.EstoqueService;
import br.edu.fag.sputnik.service.ModeloPaginaService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Gerencia consulta e cadastro do catálogo de peças. */
@Controller
public class PecaController {
    private final EstoqueService estoque;
    private final AutorizacaoService autorizacao;
    private final ModeloPaginaService pagina;

    public PecaController(
            EstoqueService estoque, AutorizacaoService autorizacao, ModeloPaginaService pagina) {
        this.estoque = estoque;
        this.autorizacao = autorizacao;
        this.pagina = pagina;
    }

    @GetMapping("/pecas")
    public String listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status,
            Model model,
            HttpSession session) {
        if (!autorizacao.temAcesso(session, PermissaoModulo.PECAS))
            return autorizacao.redirecionarSemAcesso(session);
        pagina.preparar(model, session, "pecas");
        model.addAttribute("pecas", filtrar(busca, status));
        model.addAttribute("busca", busca);
        model.addAttribute("status", status);
        return "pecas";
    }

    @GetMapping("/pecas/nova")
    public String novo(Model model, HttpSession session) {
        if (!autorizacao.temAcesso(session, PermissaoModulo.PECAS))
            return autorizacao.redirecionarSemAcesso(session);
        pagina.preparar(model, session, "nova");
        model.addAttribute("peca", new Peca());
        return "peca-form";
    }

    @PostMapping("/pecas")
    public String salvar(
            @Valid @ModelAttribute Peca peca,
            BindingResult resultado,
            Model model,
            HttpSession session) {
        if (!autorizacao.temAcesso(session, PermissaoModulo.PECAS))
            return autorizacao.redirecionarSemAcesso(session);
        if (resultado.hasErrors()) return formularioComErro(model, session);
        try {
            estoque.salvar(peca);
            return "redirect:/pecas?sucesso=cadastrada";
        } catch (IllegalArgumentException exception) {
            resultado.rejectValue("codigo", "duplicado", exception.getMessage());
            return formularioComErro(model, session);
        }
    }

    private String formularioComErro(Model model, HttpSession session) {
        pagina.preparar(model, session, "nova");
        return "peca-form";
    }

    private List<Peca> filtrar(String busca, String status) {
        var pecas = estoque.listar(busca);
        if ("baixo".equals(status)) return pecas.stream().filter(Peca::estaAbaixoDoMinimo).toList();
        if ("zerado".equals(status)) return pecas.stream().filter(Peca::estaZerada).toList();
        return pecas;
    }
}
