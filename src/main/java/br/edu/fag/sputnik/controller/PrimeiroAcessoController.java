package br.edu.fag.sputnik.controller;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import br.edu.fag.sputnik.auth.UsuarioService;
import br.edu.fag.sputnik.web.SenhaPrimeiroAcessoForm;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PrimeiroAcessoController {
    private final UsuarioService usuarios;

    public PrimeiroAcessoController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/primeiro-acesso")
    public String tela(HttpSession session, Model model) {
        var usuario = SessaoAutenticacao.usuario(session);
        if (usuario.isEmpty()) return "redirect:/login";
        if (!usuario.get().trocaSenhaObrigatoria()) return "redirect:/inicio";
        model.addAttribute("form", new SenhaPrimeiroAcessoForm());
        return "primeiro-acesso";
    }

    @PostMapping("/primeiro-acesso")
    public String salvar(
            @Valid @ModelAttribute("form") SenhaPrimeiroAcessoForm form,
            BindingResult resultado,
            HttpSession session) {
        var usuario = SessaoAutenticacao.usuario(session);
        if (usuario.isEmpty()) return "redirect:/login";
        if (!usuario.get().trocaSenhaObrigatoria()) return "redirect:/inicio";
        if (!java.util.Objects.equals(form.getSenha(), form.getConfirmacaoSenha()))
            resultado.rejectValue("confirmacaoSenha", "diferente", "As senhas não coincidem.");
        if (resultado.hasErrors()) return "primeiro-acesso";
        SessaoAutenticacao.iniciar(
                session,
                usuarios.definirSenhaDefinitiva(usuario.get().usuarioId(), form.getSenha()));
        return "redirect:/inicio?senhaAtualizada";
    }
}
