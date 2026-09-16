package br.edu.fag.sputnik.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.fag.sputnik.domain.*;

import jakarta.validation.Validation;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

class OrdemDocumentoTest {
    private final OrdemServicoPdfService service = new OrdemServicoPdfService();

    private OrdemServico fixture() {
        Cliente c = new Cliente();
        c.setNome("João da Silva");
        c.setCpf("000.000.000-00");
        c.setTelefone("(45) 99999-0000");
        ReflectionTestUtils.setField(c, "id", 1L);
        Veiculo v = new Veiculo();
        v.setMarcaModelo("Honda City Hatch EXL CVT");
        v.setPlaca("ABC1D23");
        v.setAno(2022);
        v.setChassi("9BR00000000000001");
        v.setCliente(c);
        OrdemServico o = new OrdemServico();
        ReflectionTestUtils.setField(o, "id", 42L);
        o.setCliente(c);
        o.setVeiculo(v);
        o.setStatus("ABERTA");
        o.setAbertura(LocalDateTime.of(2026, 9, 9, 14, 30));
        o.setQuilometragemAbertura(65443);
        o.setDescricao(
                "Inspeção mecânica\nRevisão de 60.000 km\nVerificar ruído na suspensão dianteira.");
        return o;
    }

    @Test
    void geraModeloComAcentosTotaisEAssinatura() throws Exception {
        OrdemServico o = fixture();
        DadosDocumentoOs d = new DadosDocumentoOs();
        d.oficinaNome = "Sputnik Oficina";
        d.oficinaEndereco = "Rua Exemplo, 100 - Centro - Cascavel / PR";
        d.oficinaContato = "(45) 3333-0000";
        d.condicaoPagamento = "PIX na retirada";
        d.validadeDias = 7;
        d.previsaoEntrega = LocalDateTime.of(2026, 9, 10, 17, 0);
        d.cor = "Branco perolizado";
        d.anoModelo = 2022;
        d.combustivelPercentual = 50;
        d.consultorTecnico = "Equipe Sputnik";
        d.tipoServico = "REVISAO";
        d.observacoes = "Cliente solicita contato antes de serviços adicionais.";
        o.setDocumento(d);
        ItemOrdemServico item = new ItemOrdemServico();
        item.setTipo(TipoItemOrdemServico.PECA);
        item.setDescricao("Filtro de óleo");
        item.setQuantidade(1);
        item.setValorUnitario(new BigDecimal("45.90"));
        byte[] pdf = service.gerar(o, List.of(item), List.of());
        String text = new String(pdf, Charset.forName("windows-1252"));
        assertThat(text)
                .startsWith("%PDF-1.4")
                .contains("João", "45,90", "Assinatura", "/Count 1", "/Subtype /Image", "/Logo");
        Path directory = Path.of("target", "pdf-qa");
        Files.createDirectories(directory);
        Files.write(directory.resolve("modelo-os.pdf"), pdf);
        Files.writeString(directory.resolve("modelo-os.html"), service.impressao(o, List.of(item)));
    }

    @Test
    void documentoAntigoSemComplementosContinuaFuncionando() {
        assertThat(service.impressao(fixture(), List.of()))
                .contains("João", "CLIENTE PAGADOR", "COMPROVANTE DE RETIRADA");
    }

    @Test
    void conteudoLongoVaiParaAnexoEHtmlEscapaDados() throws Exception {
        OrdemServico o = fixture();
        o.setDescricao(
                "<script>alert('x')</script> " + "Descrição extensa ".repeat(90) + "FIM-DO-TEXTO");
        String html = service.impressao(o, List.of());
        assertThat(html)
                .contains("&lt;script&gt;", "CONTINUAÇÃO", "FIM-DO-TEXTO")
                .doesNotContain("<script>alert");
        byte[] pdf = service.gerar(o, List.of(), List.of());
        assertThat(new String(pdf, Charset.forName("windows-1252")))
                .contains("/Count 2", "FIM-DO-TEXTO");
        Files.createDirectories(Path.of("target/pdf-qa"));
        Files.write(Path.of("target/pdf-qa/modelo-os-longa.pdf"), pdf);
    }

    @Test
    void validaCombustivelEmailEPrazos() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            DadosDocumentoOs d = new DadosDocumentoOs();
            d.combustivelPercentual = 101;
            d.validadeDias = -1;
            d.pagadorEmail = "invalido";
            assertThat(factory.getValidator().validate(d)).hasSize(3);
        }
    }
}
