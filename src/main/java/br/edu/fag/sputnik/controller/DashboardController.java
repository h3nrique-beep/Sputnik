package br.edu.fag.sputnik.controller;

import br.edu.fag.sputnik.domain.StatusOrdemServico;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.EstoqueService;
import br.edu.fag.sputnik.service.ModeloPaginaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Exibe a visão geral operacional do sistema. */
@Controller
public class DashboardController {
    private final EstoqueService estoque;
    private final AutorizacaoService autorizacao;
    private final ModeloPaginaService pagina;

    public DashboardController(
            EstoqueService estoque, AutorizacaoService autorizacao, ModeloPaginaService pagina) {
        this.estoque = estoque;
        this.autorizacao = autorizacao;
        this.pagina = pagina;
    }

    /** A rota raiz é servida pelo React; esta tela legada permanece em /inicio. */
    @GetMapping("/inicio")
    public String inicio(Model model, HttpSession session) {
        if (!autorizacao.estaLogado(session)) {
            return "redirect:/login";
        }

        pagina.preparar(model, session, "inicio");
        model.addAttribute("pecas", estoque.listar(""));
        model.addAttribute("movimentos", estoque.historico().stream().limit(5).toList());
        model.addAttribute("ordensAbertas", estoque.ordensAbertas());
        model.addAttribute("ordensPorStatus", estoque.resumoOrdensPorStatus());
        model.addAttribute("statusOrdens", StatusOrdemServico.values());
        model.addAttribute("ordensConcluidasMes", estoque.ordensConcluidasNoMes());
        model.addAttribute("aguardandoPeca", estoque.ordensAguardandoPeca());
        model.addAttribute("retornos", estoque.veiculosEmRetorno());
        model.addAttribute("pecasMaisUsadas", estoque.pecasMaisUsadas());
        if (autorizacao.ehAdministrador(session)) {
            model.addAttribute("valor", estoque.valorEstoque());
        }
        return "inicio";
    }
}
