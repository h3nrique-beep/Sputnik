package br.edu.fag.sputnik.config;

import br.edu.fag.sputnik.domain.Empresa;
import br.edu.fag.sputnik.domain.EntidadeDaEmpresa;
import br.edu.fag.sputnik.repository.*;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/** Atribui registros legados à empresa padrão antes de habilitar o filtro por empresa. */
@Configuration
public class MigracaoMultiempresa {
    @Bean
    @Order(2)
    CommandLineRunner migrarDadosLegados(EmpresaRepository empresas, ClienteRepository clientes,
            PecaRepository pecas, VeiculoRepository veiculos, OrdemServicoRepository ordens,
            MovimentacaoRepository movimentacoes, LancamentoFinanceiroRepository lancamentos) {
        return args -> migrar(empresas, clientes, pecas, veiculos, ordens, movimentacoes, lancamentos);
    }

    void migrar(EmpresaRepository empresas, ClienteRepository clientes, PecaRepository pecas,
            VeiculoRepository veiculos, OrdemServicoRepository ordens, MovimentacaoRepository movimentacoes,
            LancamentoFinanceiroRepository lancamentos) {
        if (empresas.count() == 0) return;
        Long empresaId = empresas.findAll().get(0).getId();
        clientes.saveAll(atribuirEmpresa(clientes.findAll(), empresaId));
        pecas.saveAll(atribuirEmpresa(pecas.findAll(), empresaId));
        veiculos.saveAll(atribuirEmpresa(veiculos.findAll(), empresaId));
        ordens.saveAll(atribuirEmpresa(ordens.findAll(), empresaId));
        movimentacoes.saveAll(atribuirEmpresa(movimentacoes.findAll(), empresaId));
        lancamentos.saveAll(atribuirEmpresa(lancamentos.findAll(), empresaId));
    }

    private <T extends EntidadeDaEmpresa> List<T> atribuirEmpresa(List<T> registros, Long empresaId) {
        return registros.stream()
                .filter(registro -> registro.getEmpresaId() == null)
                .map(registro -> {
                    registro.setEmpresaId(empresaId);
                    return registro;
                })
                .toList();
    }
}
