package br.edu.fag.sputnik.auth;

import br.edu.fag.sputnik.domain.Perfil;
import br.edu.fag.sputnik.domain.Empresa;
import br.edu.fag.sputnik.domain.PlanoAssinatura;
import br.edu.fag.sputnik.domain.StatusAssinatura;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.domain.UsuarioSistema;
import br.edu.fag.sputnik.repository.UsuarioSistemaRepository;
import br.edu.fag.sputnik.repository.EmpresaRepository;
import br.edu.fag.sputnik.web.UsuarioCadastroForm;
import br.edu.fag.sputnik.web.CadastroEmpresaForm;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDate;

/** Autenticação e ciclo de vida das contas de usuário. */
@Service
public class UsuarioService {
    private static final String EMAIL_BALCONISTA_PADRAO = "balconista@sputnik.com.br";
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final char[] ALFABETO_SENHA =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%*-_"
                    .toCharArray();

    private final UsuarioSistemaRepository usuarios;
    private final EmpresaRepository empresas;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioSistemaRepository usuarios, EmpresaRepository empresas, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.empresas = empresas;
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

    /** Provisiona uma nova empresa em teste com o seu administrador inicial. */
    @org.springframework.transaction.annotation.Transactional
    public UsuarioAutenticado cadastrarEmpresa(CadastroEmpresaForm form) {
        String email = normalizarLogin(form.getEmail());
        validarLoginDisponivel(email);
        validarSenhaCadastro(form.getSenha());
        String slug = form.getSlug().trim().toLowerCase(Locale.ROOT);
        if (empresas.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("Este endereço da empresa já está em uso.");
        }
        Empresa empresa = new Empresa();
        empresa.setNome(form.getEmpresaNome().trim());
        empresa.setSlug(slug);
        empresa.setPlano(PlanoAssinatura.STARTER);
        empresa.setStatusAssinatura(StatusAssinatura.EM_TESTE);
        empresa.setPeriodoTesteAte(LocalDate.now().plusDays(14));
        empresa = empresas.save(empresa);
        UsuarioSistema administrador = new UsuarioSistema();
        administrador.setNome(form.getNomeAdministrador().trim());
        administrador.setUsuario(email);
        administrador.setEmail(email);
        administrador.setSenhaHash(encoder.encode(form.getSenha()));
        administrador.setTrocaSenhaObrigatoria(false);
        administrador.setPerfil(Perfil.ADMIN);
        administrador.setPermissoes(EnumSet.allOf(PermissaoModulo.class));
        administrador.setEmpresa(empresa);
        return paraSessao(usuarios.save(administrador));
    }

    public List<UsuarioSistema> listar(Long empresaId) {
        return usuarios.findAll().stream()
                .filter(usuario -> usuario.getEmpresa() != null && usuario.getEmpresa().getId().equals(empresaId))
                .toList();
    }

    /** Compatibilidade para as telas MVC legadas; a SPA usa o escopo de empresa explícito. */
    public List<UsuarioSistema> listar() { return listar(empresaPadrao().getId()); }

    public UsuarioSistema cadastrar(UsuarioCadastroForm form, Long empresaId) {
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
        Empresa empresa = buscarEmpresa(empresaId);
        if (listar(empresa.getId()).size() >= empresa.getPlano().getLimiteUsuarios()) {
            throw new IllegalArgumentException("O plano " + empresa.getPlano() + " atingiu o limite de usuários.");
        }
        usuario.setEmpresa(empresa);
        return usuarios.save(usuario);
    }

    public UsuarioSistema cadastrar(UsuarioCadastroForm form) { return cadastrar(form, empresaPadrao().getId()); }

    public UsuarioSistema buscarBalconista(Long id, Long empresaId) {
        UsuarioSistema usuario =
                usuarios.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        Long empresaEfetiva = buscarEmpresa(empresaId).getId();
        if (usuario.getEmpresa() == null || !usuario.getEmpresa().getId().equals(empresaEfetiva)) {
            throw new IllegalArgumentException("Usuário não pertence à sua empresa.");
        }
        if (usuario.getPerfil() == Perfil.ADMIN) {
            throw new IllegalArgumentException(
                    "A conta do Administrador não pode ser alterada por esta tela.");
        }
        return usuario;
    }

    public UsuarioSistema buscarBalconista(Long id) { return buscarBalconista(id, empresaPadrao().getId()); }

    public void atualizarPermissoes(Long id, Set<PermissaoModulo> permissoes, Long empresaId) {
        UsuarioSistema usuario = buscarBalconista(id, empresaId);
        usuario.setPermissoes(copiarPermissoes(permissoes));
        usuarios.save(usuario);
        SessaoAutenticacao.revogar(id);
    }

    public void atualizarPermissoes(Long id, Set<PermissaoModulo> permissoes) { atualizarPermissoes(id, permissoes, empresaPadrao().getId()); }

    /** Retorna a senha temporária somente para que o administrador a entregue ao usuário. */
    public String resetarSenhaBalconista(Long id, Long empresaId) {
        UsuarioSistema usuario = buscarBalconista(id, empresaId);
        String senhaTemporaria = gerarSenhaTemporaria();
        usuario.setSenhaHash(encoder.encode(senhaTemporaria));
        usuario.setTrocaSenhaObrigatoria(true);
        usuarios.save(usuario);
        SessaoAutenticacao.revogar(id);
        return senhaTemporaria;
    }

    public String resetarSenhaBalconista(Long id) { return resetarSenhaBalconista(id, empresaPadrao().getId()); }

    public void excluirBalconista(Long id, Long empresaId) {
        usuarios.delete(buscarBalconista(id, empresaId));
        SessaoAutenticacao.revogar(id);
    }

    public void excluirBalconista(Long id) { excluirBalconista(id, empresaPadrao().getId()); }

    public UsuarioAutenticado definirSenhaDefinitiva(Long usuarioId, String senha) {
        UsuarioSistema usuario =
                usuarios.findById(usuarioId)
                        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        usuario.setSenhaHash(encoder.encode(senha));
        usuario.setTrocaSenhaObrigatoria(false);
        UsuarioAutenticado atualizado = paraSessao(usuarios.save(usuario));
        SessaoAutenticacao.revogar(usuarioId);
        return atualizado;
    }

    /** Cria exclusivamente o primeiro administrador com segredo fornecido pelo ambiente. */
    public void criarAdministradorInicial(String usuarioAdministrador, String senhaAdministrador) {
        if (usuarios.count() > 0) {
            return;
        }
        if (senhaAdministrador == null || senhaAdministrador.isBlank()) {
            throw new IllegalStateException(
                    "Banco sem usuários: defina SPUTNIK_BOOTSTRAP_ADMIN_PASSWORD antes de iniciar.");
        }
        validarSenhaForte(senhaAdministrador);
        String login = normalizarLogin(usuarioAdministrador == null || usuarioAdministrador.isBlank()
                ? "admin"
                : usuarioAdministrador);
        criarUsuarioPadrao(
                "Administrador",
                login,
                senhaAdministrador,
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
        usuario.setTrocaSenhaObrigatoria(true);
        usuario.setPerfil(perfil);
        usuario.setPermissoes(copiarPermissoes(permissoes));
        usuario.setEmpresa(empresaPadrao());
        usuarios.save(usuario);
    }

    public void migrarEmpresasLegadas() {
        Empresa empresa = empresaPadrao();
        usuarios.findAll().stream().filter(usuario -> usuario.getEmpresa() == null).forEach(usuario -> {
            usuario.setEmpresa(empresa);
            usuarios.save(usuario);
        });
    }

    private Empresa buscarEmpresa(Long empresaId) {
        if (empresaId == null) return empresaPadrao();
        return empresas.findById(empresaId).orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada."));
    }

    private Empresa empresaPadrao() {
        return empresas.findAll().stream().findFirst().orElseGet(() -> {
            Empresa empresa = new Empresa();
            empresa.setNome("Sputnik - Gestão de Oficina");
            empresa.setSlug("sputnik");
            empresa.setPlano(PlanoAssinatura.PREMIUM);
            empresa.setStatusAssinatura(StatusAssinatura.ATIVA);
            empresa.setPeriodoTesteAte(LocalDate.now().plusDays(14));
            return empresas.save(empresa);
        });
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

    private String gerarSenhaTemporaria() {
        char[] senha = new char[20];
        for (int i = 0; i < senha.length; i++) {
            senha[i] = ALFABETO_SENHA[ALEATORIO.nextInt(ALFABETO_SENHA.length)];
        }
        return new String(senha);
    }

    private void validarSenhaForte(String senha) {
        if (!senha.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,}")) {
            throw new IllegalStateException(
                    "SPUTNIK_BOOTSTRAP_ADMIN_PASSWORD deve ter ao menos 12 caracteres, maiúscula, minúscula, número e símbolo.");
        }
    }

    private void validarSenhaCadastro(String senha) {
        if (!senha.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,}")) {
            throw new IllegalArgumentException("A senha deve ter ao menos 12 caracteres, maiúscula, minúscula, número e símbolo.");
        }
    }

    private Set<PermissaoModulo> copiarPermissoes(Set<PermissaoModulo> permissoes) {
        return permissoes == null ? new HashSet<>() : new HashSet<>(permissoes);
    }

    private UsuarioAutenticado paraSessao(UsuarioSistema usuario) {
        return new UsuarioAutenticado(
                usuario.getId(),
                usuario.getEmpresa() == null ? null : usuario.getEmpresa().getId(),
                usuario.getEmpresa() == null ? null : usuario.getEmpresa().getNome(),
                usuario.getNome(),
                usuario.getPerfil(),
                Set.copyOf(usuario.getPermissoes()),
                usuario.isTrocaSenhaObrigatoria());
    }
}
