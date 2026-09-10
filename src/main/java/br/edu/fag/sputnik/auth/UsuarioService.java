package br.edu.fag.sputnik.auth;

import br.edu.fag.sputnik.domain.Perfil;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.domain.UsuarioSistema;
import br.edu.fag.sputnik.repository.UsuarioSistemaRepository;
import br.edu.fag.sputnik.web.UsuarioCadastroForm;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Autenticação e ciclo de vida das contas de usuário. */
@Service
public class UsuarioService {
    private static final String SENHA_TEMPORARIA_RESET = "12345678";
    private static final String EMAIL_BALCONISTA_PADRAO = "balconista@sputnik.com.br";

    private final UsuarioSistemaRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioSistemaRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    public Optional<UsuarioAutenticado> autenticar(String identificador, String senha) {
        if (identificador == null || senha == null) {
            return Optional.empty();
        }

        String loginNormalizado = normalizarLogin(identificador);
        return usuarios.findByEmailIgnoreCase(loginNormalizado)
                .or(() -> usuarios.findByUsuarioIgnoreCase(loginNormalizado))
                .filter(usuario -> encoder.matches(senha, usuario.getSenhaHash()))
                .map(this::paraSessao);
    }

    public List<UsuarioSistema> listar() {
        return usuarios.findAll();
    }

    public UsuarioSistema cadastrar(UsuarioCadastroForm form) {
        validarEmail(form.getEmail());
        String email = normalizarLogin(form.getEmail());
        validarLoginDisponivel(email);

        UsuarioSistema usuario = new UsuarioSistema();
        usuario.setNome(form.getNome().trim());
        usuario.setUsuario(email);
        usuario.setEmail(email);
        usuario.setSenhaHash(encoder.encode(form.getSenha()));
        usuario.setTrocaSenhaObrigatoria(true);
        usuario.setPerfil(Perfil.BALCONISTA);
        usuario.setPermissoes(copiarPermissoes(form.getPermissoes()));
        return usuarios.save(usuario);
    }

    public UsuarioSistema buscarBalconista(Long id) {
        UsuarioSistema usuario =
                usuarios.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (usuario.getPerfil() == Perfil.ADMIN) {
            throw new IllegalArgumentException(
                    "A conta do Administrador não pode ser alterada por esta tela.");
        }
        return usuario;
    }

    public void atualizarPermissoes(Long id, Set<PermissaoModulo> permissoes) {
        UsuarioSistema usuario = buscarBalconista(id);
        usuario.setPermissoes(copiarPermissoes(permissoes));
        usuarios.save(usuario);
    }

    public void resetarSenhaBalconista(Long id) {
        UsuarioSistema usuario = buscarBalconista(id);
        usuario.setSenhaHash(encoder.encode(SENHA_TEMPORARIA_RESET));
        usuario.setTrocaSenhaObrigatoria(true);
        usuarios.save(usuario);
    }

    public void excluirBalconista(Long id) {
        usuarios.delete(buscarBalconista(id));
    }

    public UsuarioAutenticado definirSenhaDefinitiva(Long usuarioId, String senha) {
        UsuarioSistema usuario =
                usuarios.findById(usuarioId)
                        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        usuario.setSenhaHash(encoder.encode(senha));
        usuario.setTrocaSenhaObrigatoria(false);
        return paraSessao(usuarios.save(usuario));
    }

    public void criarPadraoSeNecessario() {
        if (usuarios.count() > 0) {
            return;
        }

        criarUsuarioPadrao(
                "Balconista",
                EMAIL_BALCONISTA_PADRAO,
                "Sputnik@123",
                Perfil.BALCONISTA,
                EnumSet.of(PermissaoModulo.PECAS, PermissaoModulo.ORDENS));
        criarUsuarioPadrao(
                "Administrador",
                "admin",
                "Admin#2026!",
                Perfil.ADMIN,
                EnumSet.allOf(PermissaoModulo.class));
    }

    public void migrarBalconistaPadrao() {
        usuarios.findByUsuarioIgnoreCase("operador")
                .filter(
                        usuario ->
                                usuario.getPerfil() == Perfil.BALCONISTA
                                        && usuario.getEmail() == null)
                .ifPresent(
                        usuario -> {
                            if (!usuarios.existsByEmailIgnoreCase(EMAIL_BALCONISTA_PADRAO)) {
                                usuario.setEmail(EMAIL_BALCONISTA_PADRAO);
                                usuario.setUsuario(EMAIL_BALCONISTA_PADRAO);
                                usuarios.save(usuario);
                            }
                        });
    }

    private void criarUsuarioPadrao(
            String nome,
            String login,
            String senha,
            Perfil perfil,
            Set<PermissaoModulo> permissoes) {
        UsuarioSistema usuario = new UsuarioSistema();
        usuario.setNome(nome);
        usuario.setUsuario(login);
        usuario.setEmail(perfil == Perfil.BALCONISTA ? login : null);
        usuario.setSenhaHash(encoder.encode(senha));
        usuario.setTrocaSenhaObrigatoria(false);
        usuario.setPerfil(perfil);
        usuario.setPermissoes(copiarPermissoes(permissoes));
        usuarios.save(usuario);
    }

    private void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do Balconista.");
        }
    }

    private void validarLoginDisponivel(String email) {
        if (usuarios.existsByEmailIgnoreCase(email) || usuarios.existsByUsuarioIgnoreCase(email)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        }
    }

    private String normalizarLogin(String login) {
        return login.trim().toLowerCase(Locale.ROOT);
    }

    private Set<PermissaoModulo> copiarPermissoes(Set<PermissaoModulo> permissoes) {
        return permissoes == null ? new HashSet<>() : new HashSet<>(permissoes);
    }

    private UsuarioAutenticado paraSessao(UsuarioSistema usuario) {
        return new UsuarioAutenticado(
                usuario.getId(),
                usuario.getNome(),
                usuario.getPerfil(),
                Set.copyOf(usuario.getPermissoes()),
                usuario.isTrocaSenhaObrigatoria());
    }
}
