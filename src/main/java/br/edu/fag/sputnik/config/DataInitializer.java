package br.edu.fag.sputnik.config;

import br.edu.fag.sputnik.domain.Peca;
import br.edu.fag.sputnik.repository.PecaRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/** Insere dados de demonstração somente em uma base ainda vazia. */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner iniciarDadosDeDemonstracao(PecaRepository pecas) {
        return args -> {
            if (pecas.count() > 0) {
                return;
            }

            adicionar(
                    pecas,
                    "FO-102",
                    "Filtro de Óleo",
                    "Tecfil",
                    "Filtros",
                    38.90,
                    "Corredor B · Prateleira 4",
                    5,
                    8,
                    "Tecfil");
            adicionar(
                    pecas,
                    "PF-220",
                    "Pastilha de Freio",
                    "Bosch",
                    "Freios",
                    89,
                    "Corredor A · Prateleira 2",
                    12,
                    6,
                    "Autopeças Sul");
            adicionar(
                    pecas,
                    "CD-310",
                    "Correia Dentada",
                    "Continental",
                    "Motor",
                    145,
                    "Corredor C · Prateleira 1",
                    0,
                    5,
                    "Autopeças Sul");
            adicionar(
                    pecas,
                    "VI-118",
                    "Vela de Ignição",
                    "NGK",
                    "Ignição",
                    32.5,
                    "Corredor B · Prateleira 1",
                    2,
                    8,
                    "Cofap");
            adicionar(
                    pecas,
                    "FA-090",
                    "Filtro de Ar",
                    "Tecfil",
                    "Filtros",
                    54.9,
                    "Corredor B · Prateleira 3",
                    3,
                    10,
                    "Tecfil");
            adicionar(
                    pecas,
                    "AD-405",
                    "Amortecedor Dianteiro",
                    "Cofap",
                    "Suspensão",
                    210,
                    "Corredor C · Prateleira 2",
                    7,
                    4,
                    "Cofap");
        };
    }

    private void adicionar(
            PecaRepository pecas,
            String codigo,
            String nome,
            String fabricante,
            String categoria,
            double preco,
            String locacao,
            int saldo,
            int estoqueMinimo,
            String fornecedor) {
        Peca peca = new Peca();
        peca.setCodigo(codigo);
        peca.setNome(nome);
        peca.setFabricante(fabricante);
        peca.setCategoria(categoria);
        peca.setPreco(BigDecimal.valueOf(preco));
        peca.setLocalizacao(locacao);
        peca.setSaldo(saldo);
        peca.setEstoqueDisponivel(saldo);
        peca.setEstoqueMinimo(estoqueMinimo);
        peca.setFornecedor(fornecedor);
        pecas.save(peca);
    }
}
