package br.edu.fag.sputnik.service;

import br.edu.fag.sputnik.domain.*;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Um único layout A4 alimenta a impressão HTML e o PDF. */
@Service
public class OrdemServicoPdfService {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] gerar(
            OrdemServico ordem,
            List<ItemOrdemServico> itens,
            List<HistoricoOrdemServico> historico) {
        return montar(ordem, itens, historico).pdf();
    }

    public String impressao(OrdemServico ordem, List<ItemOrdemServico> itens) {
        return impressao(ordem, itens, List.of());
    }

    public String impressao(
            OrdemServico ordem,
            List<ItemOrdemServico> itens,
            List<HistoricoOrdemServico> historico) {
        return montar(ordem, itens, historico).html("Ordem de serviço " + ordem.getId());
    }

    private DocumentoOsLayout montar(
            OrdemServico o, List<ItemOrdemServico> itens, List<HistoricoOrdemServico> historico) {
        DocumentoOsLayout d = new DocumentoOsLayout();
        DadosDocumentoOs c = o.getDocumento() == null ? new DadosDocumentoOs() : o.getDocumento();
        Cliente dono = o.getCliente();
        Veiculo v = o.getVeiculo();
        d.box(24, 24, 547, 48, "", List.of());
        d.text(36, 39, 20, true, "SPUTNIK");
        d.text(36, 61, 8, false, "GESTÃO DE OFICINA");
        d.text(337, 40, 14, true, "ORDEM DE SERVIÇO #" + o.getId());
        d.text(
                337,
                59,
                8,
                false,
                "Via da oficina / cliente - " + valor(o.getStatus()).replace('_', ' '));
        d.box(
                24,
                76,
                290,
                91,
                "OFICINA",
                List.of(
                        padrao(c.oficinaNome, "Sputnik - Gestão de Oficina"),
                        "Endereço: " + valor(c.oficinaEndereco),
                        "Contato: " + valor(c.oficinaContato),
                        "CNPJ / CPF: " + valor(c.oficinaDocumento)));
        d.box(
                314,
                76,
                257,
                91,
                "DADOS DA ORDEM",
                List.of(
                        "Emissão: " + data(o.getAbertura()),
                        "Pagamento: " + valor(c.condicaoPagamento),
                        "Validade: " + valor(c.validadeDias) + " dia(s)",
                        "Entrega prevista: " + data(c.previsaoEntrega),
                        "Orçamento previsto: " + data(c.previsaoOrcamento)));
        boolean mesmoPagador = c.pagadorNome == null || c.pagadorNome.isBlank();
        d.box(
                24,
                171,
                350,
                96,
                "CLIENTE PAGADOR",
                List.of(
                        "Nome: " + (mesmoPagador ? dono.getNome() : c.pagadorNome),
                        "CPF / CNPJ: "
                                + (mesmoPagador ? valor(dono.getCpf()) : valor(c.pagadorDocumento)),
                        "Telefone: "
                                + (mesmoPagador
                                        ? valor(dono.getTelefone())
                                        : valor(c.pagadorTelefone)),
                        "E-mail: " + valor(mesmoPagador ? c.proprietarioEmail : c.pagadorEmail),
                        "Endereço: "
                                + valor(
                                        mesmoPagador
                                                ? c.proprietarioEndereco
                                                : c.pagadorEndereco)));
        d.box(
                24,
                267,
                350,
                96,
                "PROPRIETÁRIO / CLIENTE VINCULADO",
                List.of(
                        "Nome: " + dono.getNome() + "   Código: " + dono.getId(),
                        "CPF / CNPJ: " + valor(dono.getCpf()),
                        "Telefone: " + valor(dono.getTelefone()),
                        "E-mail: " + valor(c.proprietarioEmail),
                        "Endereço: " + valor(c.proprietarioEndereco)));
        d.box(
                374,
                171,
                197,
                192,
                "DADOS DO VEÍCULO",
                List.of(
                        "Modelo: " + v.getMarcaModelo(),
                        "Placa: " + v.getPlaca(),
                        "Chassi: " + v.getChassi(),
                        "Motor: " + valor(c.motor),
                        "Ano / modelo: " + valor(v.getAno()) + " / " + valor(c.anoModelo),
                        "Cor: " + valor(c.cor),
                        "KM de entrada: " + valor(o.getQuilometragemAbertura()),
                        "Combustível: "
                                + valor(c.combustivelPercentual)
                                + (c.combustivelPercentual == null ? "" : "%"),
                        "Tipo: " + padrao(c.tipoServico, "EXTERNO"),
                        "Prisma / cor: " + valor(c.prisma),
                        "Consultor: " + valor(c.consultorTecnico),
                        "Garantia: " + valor(c.certificadoGarantia)));
        d.box(
                24,
                363,
                350,
                148,
                "SERVIÇOS A EXECUTAR / SOLICITAÇÃO",
                List.of(valor(o.getDescricao())));
        List<String> pecas =
                itens.stream()
                        .filter(i -> i.getTipo() == TipoItemOrdemServico.PECA)
                        .map(
                                i ->
                                        i.getQuantidade()
                                                + " x "
                                                + i.getDescricao()
                                                + " | "
                                                + moeda(i.getTotal()))
                        .toList();
        d.box(
                374,
                363,
                197,
                148,
                "REQUISIÇÃO DE PEÇAS",
                pecas.isEmpty() ? List.of("Nenhuma peça lançada.") : pecas);
        d.box(24, 511, 547, 68, "OBSERVAÇÕES", List.of(valor(c.observacoes)));
        List<String> servicos =
                itens.stream()
                        .filter(i -> i.getTipo() == TipoItemOrdemServico.SERVICO)
                        .map(i -> i.getDescricao() + " | " + moeda(i.getTotal()))
                        .toList();
        d.box(
                24,
                579,
                350,
                82,
                "SERVIÇOS EXECUTADOS / MÃO DE OBRA",
                servicos.isEmpty() ? List.of("Nenhum serviço lançado.") : servicos);
        BigDecimal totalPecas = total(itens, TipoItemOrdemServico.PECA);
        BigDecimal totalServicos = total(itens, TipoItemOrdemServico.SERVICO);
        d.box(
                374,
                579,
                197,
                82,
                "DESPESAS",
                List.of(
                        "Peças: " + moeda(totalPecas),
                        "Serviços / mão de obra: " + moeda(totalServicos),
                        "TOTAL: " + moeda(totalPecas.add(totalServicos))));
        d.box(24, 661, 547, 32, "GARANTIA / REEMBOLSO (SE APLICÁVEL)", List.of(valor(c.reembolso)));
        d.box(
                24,
                693,
                350,
                69,
                "CIÊNCIA DO CLIENTE",
                List.of("Declaro ciência dos serviços e valores descritos nesta ordem."));
        d.line(38, 747, 360, 747);
        d.text(108, 751, 8, false, "Assinatura do cliente");
        d.box(374, 693, 197, 69, "RETIRADA DO VEÍCULO", List.of("Data: " + data(c.retiradaEm)));
        d.line(386, 747, 559, 747);
        d.text(400, 751, 8, false, "Assinatura / conferência");
        d.box(
                24,
                773,
                547,
                45,
                "COMPROVANTE DE RETIRADA - OS #" + o.getId(),
                List.of(
                        "Placa: "
                                + v.getPlaca()
                                + " | Entrega: "
                                + data(c.previsaoEntrega)
                                + " | Prisma: "
                                + valor(c.prisma),
                        "Apresente este comprovante. Consultor: " + valor(c.consultorTecnico)));
        d.appendix(
                "HISTÓRICO DO ATENDIMENTO",
                historico.stream()
                        .map(
                                h ->
                                        data(h.getData())
                                                + " | "
                                                + valor(h.getResponsavel())
                                                + " | "
                                                + valor(h.getDescricao()))
                        .toList());
        d.finish(o.getId());
        return d;
    }

    private static BigDecimal total(List<ItemOrdemServico> itens, TipoItemOrdemServico tipo) {
        return itens.stream()
                .filter(i -> i.getTipo() == tipo)
                .map(ItemOrdemServico::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String moeda(BigDecimal n) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(n);
    }

    private static String valor(Object v) {
        return v == null || v.toString().isBlank() ? "-" : v.toString();
    }

    private static String padrao(String v, String fallback) {
        return v == null || v.isBlank() ? fallback : v;
    }

    private static String data(LocalDateTime d) {
        return d == null ? "____/____/________" : DATA.format(d);
    }
}
