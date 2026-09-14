package br.edu.fag.sputnik.config;

import br.edu.fag.sputnik.domain.Empresa;
import br.edu.fag.sputnik.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    void migrar(EmpresaRepository empresas, ClienteRepository clientes, PecaRepository pecas,
            VeiculoRepository veiculos, OrdemServicoRepository ordens, MovimentacaoRepository movimentacoes,
            LancamentoFinanceiroRepository lancamentos) {
        if (empresas.count() == 0) return;
        Long empresaId = empresas.findAll().get(0).getId();
        clientes.findAll().stream().filter(x -> x.getEmpresaId() == null).forEach(x -> x.setEmpresaId(empresaId));
        pecas.findAll().stream().filter(x -> x.getEmpresaId() == null).forEach(x -> x.setEmpresaId(empresaId));
        veiculos.findAll().stream().filter(x -> x.getEmpresaId() == null).forEach(x -> x.setEmpresaId(empresaId));
        ordens.findAll().stream().filter(x -> x.getEmpresaId() == null).forEach(x -> x.setEmpresaId(empresaId));
        movimentacoes.findAll().stream().filter(x -> x.getEmpresaId() == null).forEach(x -> x.setEmpresaId(empresaId));
        lancamentos.findAll().stream().filter(x -> x.getEmpresaId() == null).forEach(x -> x.setEmpresaId(empresaId));
    }
}
