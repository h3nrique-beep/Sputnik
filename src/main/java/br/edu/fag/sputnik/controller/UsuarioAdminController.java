package br.edu.fag.sputnik.controller;

import br.edu.fag.sputnik.auth.UsuarioService;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.ModeloPaginaService;
import br.edu.fag.sputnik.web.UsuarioCadastroForm;
import br.edu.fag.sputnik.web.UsuarioPermissoesForm;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Administração de contas de Balconista e das permissões de módulos. */
@Controller
public class UsuarioAdminController {
    private final UsuarioService usuarios;
    private final AutorizacaoService autorizacao;
    private final ModeloPaginaService pagina;

    public UsuarioAdminController(
            UsuarioService usuarios, AutorizacaoService autorizacao, ModeloPaginaService pagina) {
        this.usuarios = usuarios;
        this.autorizacao = autorizacao;
        this.pagina = pagina;
    }

    @GetMapping("/admin/usuarios")
    public String listar(Model model, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) {
            return autorizacao.redirecionarSemAcesso(session);
        }

        pagina.preparar(model, session, "usuarios");
        model.addAttribute("usuarios", usuarios.listar(empresaId(session)));
        return "usuarios";
    }

    @GetMapping("/admin/usuarios/novo")
    public String novo(Model model, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) {
            return autorizacao.redirecionarSemAcesso(session);
        }

        prepararFormularioCadastro(model, session);
        model.addAttribute("form", new UsuarioCadastroForm());
        return "usuario-form";
    }

    @PostMapping("/admin/usuarios")
    public String cadastrar(
            @Valid @ModelAttribute("form") UsuarioCadastroForm form,
            BindingResult resultado,
            Model model,
            HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) {
            return autorizacao.redirecionarSemAcesso(session);
        }
        if (!resultado.hasErrors()) {
            try {
                usuarios.cadastrar(form, empresaId(session));
                return "redirect:/admin/usuarios?sucesso=usuario";
            } catch (IllegalArgumentException exception) {
                resultado.reject("cadastro", exception.getMessage());
            }
        }

        prepararFormularioCadastro(model, session);
        return "usuario-form";
    }

    @GetMapping("/admin/usuarios/{id}/permissoes")
    public String editarPermissoes(@PathVariable Long id, Model model, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) {
            return autorizacao.redirecionarSemAcesso(session);
        }

        try {
            var usuario = usuarios.buscarBalconista(id, empresaId(session));
            var form = new UsuarioPermissoesForm();
            form.setPermissoes(usuario.getPermissoes());

            pagina.preparar(model, session, "usuarios");
            model.addAttribute("usuarioEditado", usuario);
            model.addAttribute("form", form);
            model.addAttribute("permissoes", PermissaoModulo.values());
            return "usuario-permissoes";
        } catch (IllegalArgumentException exception) {
            return "redirect:/admin/usuarios?erro=usuario";
        }
    }

    @PostMapping("/admin/usuarios/{id}/permissoes")
    public String salvarPermissoes(
            @PathVariable Long id,
            @ModelAttribute("form") UsuarioPermissoesForm form,
            HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) {
            return autorizacao.redirecionarSemAcesso(session);
        }

        try {
            usuarios.atualizarPermissoes(id, form.getPermissoes(), empresaId(session));
            return "redirect:/admin/usuarios?sucesso=permissoes";
        } catch (IllegalArgumentException exception) {
            return "redirect:/admin/usuarios?erro=usuario";
        }
    }

    @PostMapping("/admin/usuarios/{id}/resetar-senha")
    public String resetarSenha(
            @PathVariable Long id, HttpSession session, RedirectAttributes atributos) {
        if (!autorizacao.ehAdministrador(session)) {
            return autorizacao.redirecionarSemAcesso(session);
        }

        try {
            atributos.addFlashAttribute(
                    "senhaTemporaria", usuarios.resetarSenhaBalconista(id, empresaId(session)));
            return "redirect:/admin/usuarios?sucesso=senhaResetada";
        } catch (IllegalArgumentException exception) {
            return "redirect:/admin/usuarios?erro=usuario";
        }
    }

    @PostMapping("/admin/usuarios/{id}/excluir")
    public String excluir(@PathVariable Long id, HttpSession session) {
        if (!autorizacao.ehAdministrador(session)) {
            return autorizacao.redirecionarSemAcesso(session);
        }

        try {
            usuarios.excluirBalconista(id, empresaId(session));
            return "redirect:/admin/usuarios?sucesso=excluido";
        } catch (IllegalArgumentException exception) {
            return "redirect:/admin/usuarios?erro=usuario";
        }
    }

    private void prepararFormularioCadastro(Model model, HttpSession session) {
        pagina.preparar(model, session, "usuarios");
        model.addAttribute("permissoes", PermissaoModulo.values());
    }

    private Long empresaId(HttpSession session) {
        return autorizacao.usuarioAtual(session).orElseThrow().empresaId();
    }
}
