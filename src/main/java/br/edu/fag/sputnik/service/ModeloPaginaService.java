package br.edu.fag.sputnik.service;

import br.edu.fag.sputnik.domain.PermissaoModulo;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/** Preenche os dados compartilhados por todas as páginas autenticadas. */
@Service
public class ModeloPaginaService {
    private final EstoqueService estoque;
    private final AutorizacaoService autorizacao;

    public ModeloPaginaService(EstoqueService estoque, AutorizacaoService autorizacao) {
        this.estoque = estoque;
        this.autorizacao = autorizacao;
    }

    public void preparar(Model model, HttpSession session, String pagina) {
        model.addAttribute("pagina", pagina);
        autorizacao
                .usuarioAtual(session)
                .ifPresent(
                        usuario -> {
                            model.addAttribute("nome", usuario.nomeExibicao());
                            model.addAttribute("perfil", usuario.perfil());
                            model.addAttribute(
                                    "administrador", autorizacao.ehAdministrador(session));
                            model.addAttribute(
                                    "podePecas",
                                    autorizacao.temAcesso(session, PermissaoModulo.PECAS));
                            model.addAttribute(
                                    "podeOrdens",
                                    autorizacao.temAcesso(session, PermissaoModulo.ORDENS));
                            model.addAttribute(
                                    "podeRelatorio",
                                    autorizacao.temAcesso(
                                            session, PermissaoModulo.RELATORIO_MENSAL));
                            model.addAttribute(
                                    "podeReposicao",
                                    autorizacao.temAcesso(session, PermissaoModulo.REPOSICAO));
                        });
        model.addAttribute("criticas", estoque.criticas());
    }
}
