package br.edu.fag.sputnik.tenant;

import br.edu.fag.sputnik.domain.EntidadeDaEmpresa;

/** Empresa da requisição atual; usada para gravar e filtrar dados operacionais. */
public final class EmpresaContext {
    private static final ThreadLocal<Long> EMPRESA_ID = new ThreadLocal<>();
    private EmpresaContext() {}
    public static Long empresaId() { return EMPRESA_ID.get(); }
    public static void definir(Long empresaId) { if (empresaId != null) EMPRESA_ID.set(empresaId); }
    public static void limpar() { EMPRESA_ID.remove(); }

    /** Protege buscas diretas por ID, que não recebem filtro Hibernate automaticamente. */
    public static <T extends EntidadeDaEmpresa> T exigirPertencimento(T entidade, String mensagem) {
        Long atual = empresaId();
        if (atual != null && !atual.equals(entidade.getEmpresaId())) throw new IllegalArgumentException(mensagem);
        return entidade;
    }
}
