package br.edu.fag.sputnik.controller;

import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.domain.StatusOrdemServico;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.EstoqueService;
import br.edu.fag.sputnik.service.ModeloPaginaService;
import br.edu.fag.sputnik.web.ItemPecaOrdemForm;
import br.edu.fag.sputnik.web.OrdemServicoEdicaoForm;
import br.edu.fag.sputnik.web.OrdemServicoForm;
import br.edu.fag.sputnik.web.ServicoOrdemForm;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;

/** Controla o fluxo de seleção e abertura das ordens de serviço. */
@Controller
public class OrdemServicoController {
    private final EstoqueService estoque;
    private final AutorizacaoService autorizacao;
    private final ModeloPaginaService pagina;

    public OrdemServicoController(
            EstoqueService estoque, AutorizacaoService autorizacao, ModeloPaginaService pagina) {
        this.estoque = estoque;
        this.autorizacao = autorizacao;
        this.pagina = pagina;
    }

    @GetMapping("/ordens")
    public String listar(Model model, HttpSession session) {
        if (!podeAbrirOrdem(session)) return semAcesso(session);
        pagina.preparar(model, session, "ordens");
        model.addAttribute("ordens", estoque.ordens());
        return "ordens";
    }

    @GetMapping("/ordens/nova")
    public String nova(Model model, HttpSession session) {
        if (!podeAbrirOrdem(session)) return semAcesso(session);
        pagina.preparar(model, session, "ordens");
        model.addAttribute("form", new OrdemServicoForm());
        adicionarOpcoes(model);
        return "ordem-form";
    }

    @PostMapping("/ordens")
    public String abrir(
            @Valid @ModelAttribute("form") OrdemServicoForm formulario,
            BindingResult resultado,
            Model model,
            HttpSession session) {
        if (!podeAbrirOrdem(session)) return semAcesso(session);
        if (!resultado.hasErrors()) {
            try {
                estoque.abrirOrdem(
                        formulario.getClienteId(),
                        formulario.getVeiculoId(),
                        formulario.getDescricao(),
                        formulario.getQuilometragemAtual(),
                        operador(session));
                return "redirect:/ordens?sucesso=ordem";
            } catch (IllegalArgumentException exception) {
                resultado.reject("ordem", exception.getMessage());
            }
        }
        pagina.preparar(model, session, "ordens");
        adicionarOpcoes(model);
        return "ordem-form";
    }

    @GetMapping("/ordens/{id}")
    public String detalhe(@PathVariable Long id, Model model, HttpSession session) {
        if (!podeAbrirOrdem(session)) return semAcesso(session);
        try {
            prepararDetalhe(id, model, session);
            return "ordem-detalhe";
        } catch (IllegalArgumentException exception) {
            return "redirect:/ordens?erro=ordem";
        }
    }

    @GetMapping("/ordens/{id}/editar")
    public String editar(@PathVariable Long id, Model model, HttpSession session) {
        if (!podeAbrirOrdem(session)) return semAcesso(session);
        try {
            var ordem = estoque.buscarOrdem(id);
            OrdemServicoEdicaoForm form = new OrdemServicoEdicaoForm();
            form.setDescricao(ordem.getDescricao());
            form.setQuilometragemAtual(ordem.getQuilometragemAbertura());
            form.setStatus(StatusOrdemServico.valueOf(ordem.getStatus()));
            pagina.preparar(model, session, "ordens");
            model.addAttribute("ordem", ordem);
            model.addAttribute("form", form);
            model.addAttribute("statusDisponiveis", StatusOrdemServico.values());
            return "ordem-editar";
        } catch (IllegalArgumentException exception) {
            return "redirect:/ordens?erro=ordem";
        }
    }

    @PostMapping("/ordens/{id}/editar")
    public String salvarEdicao(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") OrdemServicoEdicaoForm form,
            BindingResult resultado,
            Model model,
            HttpSession session) {
        if (!podeAbrirOrdem(session)) return semAcesso(session);
        if (!resultado.hasErrors()) {
            try {
                estoque.atualizarOrdem(id, form, operador(session));
                return "redirect:/ordens/" + id + "?sucesso=atualizada";
            } catch (IllegalArgumentException exception) {
                resultado.reject("ordem", exception.getMessage());
            }
        }
        pagina.preparar(model, session, "ordens");
        model.addAttribute("ordem", estoque.buscarOrdem(id));
        model.addAttribute("statusDisponiveis", StatusOrdemServico.values());
        return "ordem-editar";
    }

    @PostMapping("/ordens/{id}/itens/peca")
    public String adicionarPeca(
            @PathVariable Long id,
            @Valid @ModelAttribute ItemPecaOrdemForm form,
            BindingResult resultado,
            HttpSession session) {
        if (!podeAbrirOrdem(session)) return semAcesso(session);
        if (resultado.hasErrors()) return "redirect:/ordens/" + id + "?erro=item";
        try {
            estoque.adicionarPecaNaOrdem(
                    id, form.getPecaId(), form.getQuantidade(), operador(session));
            return "redirect:/ordens/" + id + "?sucesso=peca";
        } catch (IllegalArgumentException exception) {
            return "redirect:/ordens/" + id + "?erro=item";
        }
    }

    @PostMapping("/ordens/{id}/itens/servico")
    public String adicionarServico(
            @PathVariable Long id,
            @Valid @ModelAttribute ServicoOrdemForm form,
            BindingResult resultado,
            HttpSession session) {
        if (!podeAbrirOrdem(session)) return semAcesso(session);
        if (resultado.hasErrors()) return "redirect:/ordens/" + id + "?erro=item";
        try {
            estoque.adicionarServicoNaOrdem(
                    id, form.getDescricao(), form.getValor(), operador(session));
            return "redirect:/ordens/" + id + "?sucesso=servico";
        } catch (IllegalArgumentException exception) {
            return "redirect:/ordens/" + id + "?erro=item";
        }
    }

    private void adicionarOpcoes(Model model) {
        model.addAttribute("pessoas", estoque.clientes());
        model.addAttribute("veiculos", estoque.veiculosVinculados());
    }

    private void prepararDetalhe(Long id, Model model, HttpSession session) {
        var ordem = estoque.buscarOrdem(id);
        var itens = estoque.itensDaOrdem(id);
        pagina.preparar(model, session, "ordens");
        model.addAttribute("ordem", ordem);
        model.addAttribute("itens", itens);
        model.addAttribute("historico", estoque.historicoDaOrdem(id));
        BigDecimal totalPecas =
                itens.stream()
                        .filter(item -> item.getTipo().name().equals("PECA"))
                        .map(item -> item.getTotal())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalServicos =
                itens.stream()
                        .filter(item -> item.getTipo().name().equals("SERVICO"))
                        .map(item -> item.getTotal())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalPecas", totalPecas);
        model.addAttribute("totalServicos", totalServicos);
        model.addAttribute("totalGeral", totalPecas.add(totalServicos));
        model.addAttribute("pecas", estoque.listar(""));
        model.addAttribute("pecaForm", new ItemPecaOrdemForm());
        model.addAttribute("servicoForm", new ServicoOrdemForm());
        model.addAttribute(
                "encerrada", StatusOrdemServico.valueOf(ordem.getStatus()).encerraAtendimento());
    }

    private String operador(HttpSession session) {
        return autorizacao.usuarioAtual(session).orElseThrow().nomeExibicao();
    }

    private boolean podeAbrirOrdem(HttpSession session) {
        return autorizacao.temAcesso(session, PermissaoModulo.ORDENS);
    }

    private String semAcesso(HttpSession session) {
        return autorizacao.redirecionarSemAcesso(session);
    }
}
