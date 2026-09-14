package br.edu.fag.sputnik.tenant;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;
import jakarta.persistence.EntityManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Ativa o escopo da empresa para toda requisição autenticada. */
@Component
public class EmpresaContextFilter extends OncePerRequestFilter {
    private final EntityManager entityManager;
    public EmpresaContextFilter(EntityManager entityManager) { this.entityManager = entityManager; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Long empresaId = request.getSession(false) == null ? null
                : SessaoAutenticacao.usuario(request.getSession(false)).map(u -> u.empresaId()).orElse(null);
        Session session = entityManager.unwrap(Session.class);
        if (empresaId != null) {
            EmpresaContext.definir(empresaId);
            session.enableFilter("empresaAtual").setParameter("empresaId", empresaId);
        }
        try { chain.doFilter(request, response); }
        finally {
            if (empresaId != null) session.disableFilter("empresaAtual");
            EmpresaContext.limpar();
        }
    }
}
