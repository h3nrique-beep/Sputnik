package br.edu.fag.sputnik.tenant;

import br.edu.fag.sputnik.auth.SessaoAutenticacao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Ativa o escopo da empresa para toda requisição autenticada. */
@Component
public class EmpresaContextFilter extends OncePerRequestFilter {
    private final EntityManagerFactory entityManagerFactory;

    public EmpresaContextFilter(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Long empresaId =
                request.getSession(false) == null
                        ? null
                        : SessaoAutenticacao.usuario(request.getSession(false))
                                .map(u -> u.empresaId())
                                .orElse(null);
        if (empresaId == null) {
            try {
                chain.doFilter(request, response);
            } finally {
                EmpresaContext.limpar();
            }
            return;
        }
        // O proxy de EntityManager fora de uma transação usa uma sessão temporária.
        // Vincule a sessão filtrada para que os repositórios realmente a reutilizem.
        var existente =
                (EntityManagerHolder)
                        TransactionSynchronizationManager.getResource(entityManagerFactory);
        EntityManager em =
                existente == null
                        ? entityManagerFactory.createEntityManager()
                        : existente.getEntityManager();
        Session session = em.unwrap(Session.class);
        try {
            if (existente == null) {
                TransactionSynchronizationManager.bindResource(
                        entityManagerFactory, new EntityManagerHolder(em));
            }
            EmpresaContext.definir(empresaId);
            session.enableFilter("empresaAtual").setParameter("empresaId", empresaId);
            chain.doFilter(request, response);
        } finally {
            EmpresaContext.limpar();
            try {
                session.disableFilter("empresaAtual");
            } finally {
                if (existente == null) {
                    TransactionSynchronizationManager.unbindResource(entityManagerFactory);
                    em.close();
                }
            }
        }
    }
}
