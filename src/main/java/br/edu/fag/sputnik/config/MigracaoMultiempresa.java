package br.edu.fag.sputnik.config;

import br.edu.fag.sputnik.domain.Empresa;
import br.edu.fag.sputnik.repository.*;
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
        clientes.saveAll(clientes.findAll().stream().filter(x -> x.getEmpresaId() == null).peek(x -> x.setEmpresaId(empresaId)).toList());
        pecas.saveAll(pecas.findAll().stream().filter(x -> x.getEmpresaId() == null).peek(x -> x.setEmpresaId(empresaId)).toList());
        veiculos.saveAll(veiculos.findAll().stream().filter(x -> x.getEmpresaId() == null).peek(x -> x.setEmpresaId(empresaId)).toList());
        ordens.saveAll(ordens.findAll().stream().filter(x -> x.getEmpresaId() == null).peek(x -> x.setEmpresaId(empresaId)).toList());
        movimentacoes.saveAll(movimentacoes.findAll().stream().filter(x -> x.getEmpresaId() == null).peek(x -> x.setEmpresaId(empresaId)).toList());
        lancamentos.saveAll(lancamentos.findAll().stream().filter(x -> x.getEmpresaId() == null).peek(x -> x.setEmpresaId(empresaId)).toList());
    }
}
