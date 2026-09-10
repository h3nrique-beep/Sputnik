package br.edu.fag.sputnik.controller;

import br.edu.fag.sputnik.domain.Cliente;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.domain.Veiculo;
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

/** Centraliza os cadastros de clientes e veículos. */
@Controller
public class CadastroController {
    private final EstoqueService estoque;
    private final AutorizacaoService autorizacao;
    private final ModeloPaginaService pagina;

    public CadastroController(
            EstoqueService estoque, AutorizacaoService autorizacao, ModeloPaginaService pagina) {
        this.estoque = estoque;
        this.autorizacao = autorizacao;
        this.pagina = pagina;
    }

    @GetMapping("/cadastros")
    public String cadastros() {
        return "redirect:/clientes";
    }

    @GetMapping("/clientes")
    public String clientes(
            @RequestParam(required = false) String busca, Model model, HttpSession session) {
        if (!podeGerenciar(session)) return semAcesso(session);
        pagina.preparar(model, session, "clientes");
        model.addAttribute("busca", busca);
        model.addAttribute("clientes", estoque.clientes(busca));
        return "clientes";
    }

    @GetMapping({"/clientes/novo", "/pessoas/nova"})
    public String novoCliente(Model model, HttpSession session) {
        if (!podeGerenciar(session)) return semAcesso(session);
        pagina.preparar(model, session, "novo-cliente");
        model.addAttribute("pessoa", new Cliente());
        return "pessoa-form";
    }

    @PostMapping({"/clientes", "/pessoas"})
    public String salvarCliente(
            @Valid @ModelAttribute("pessoa") Cliente cliente,
            BindingResult resultado,
            Model model,
            HttpSession session) {
        if (!podeGerenciar(session)) return semAcesso(session);
        if (resultado.hasErrors()) {
            pagina.preparar(model, session, "novo-cliente");
            return "pessoa-form";
        }
        estoque.salvarCliente(cliente);
        return "redirect:/clientes?sucesso=cliente";
    }

    @GetMapping("/veiculos")
    public String veiculos(
            @RequestParam(required = false) String busca, Model model, HttpSession session) {
        if (!podeGerenciar(session)) return semAcesso(session);
        pagina.preparar(model, session, "veiculos");
        model.addAttribute("busca", busca);
        model.addAttribute("veiculos", estoque.veiculos(busca));
        return "veiculos";
    }

    @GetMapping("/veiculos/novo")
    public String novoVeiculo(
            @RequestParam(required = false) Long cliente, Model model, HttpSession session) {
        if (!podeGerenciar(session)) return semAcesso(session);
        prepararFormularioVeiculo(model, session);
        model.addAttribute("veiculo", new Veiculo());
        model.addAttribute("clienteSelecionado", cliente);
        return "veiculo-form";
    }

    @PostMapping("/veiculos")
    public String salvarVeiculo(
            @Valid @ModelAttribute Veiculo veiculo,
            BindingResult resultado,
            @RequestParam(required = false) Long clienteId,
            Model model,
            HttpSession session) {
        if (!podeGerenciar(session)) return semAcesso(session);
        if (resultado.hasErrors()) {
            prepararFormularioVeiculo(model, session);
            return "veiculo-form";
        }
        estoque.salvarVeiculo(veiculo, clienteId);
        return "redirect:/veiculos?sucesso=veiculo";
    }

    private void prepararFormularioVeiculo(Model model, HttpSession session) {
        pagina.preparar(model, session, "novo-veiculo");
        model.addAttribute("pessoas", estoque.clientes());
    }

    private boolean podeGerenciar(HttpSession session) {
        return autorizacao.temAcesso(session, PermissaoModulo.ORDENS);
    }

    private String semAcesso(HttpSession session) {
        return autorizacao.redirecionarSemAcesso(session);
    }
}
