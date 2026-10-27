package br.edu.fag.sputnik.controller.api;

import br.edu.fag.sputnik.controller.api.mapper.ApiMapper;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.OrdensService;
import br.edu.fag.sputnik.service.PecasService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardApiController extends ApiSupport {
    private final PecasService pecasService;
    private final OrdensService ordensService;

    public DashboardApiController(
            AutorizacaoService autorizacao,
            PecasService pecasService,
            OrdensService ordensService) {
        super(autorizacao);
        this.pecasService = pecasService;
        this.ordensService = ordensService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpSession session) {
        if (!autorizacao.estaLogado(session)) return naoAutorizado();
        if (!temAcesso(session, PermissaoModulo.PECAS)
                && !temAcesso(session, PermissaoModulo.ORDENS)
                && !temAcesso(session, PermissaoModulo.RELATORIO_MENSAL)
                && !temAcesso(session, PermissaoModulo.REPOSICAO)) return proibido();
        Map<String, Object> resposta = new LinkedHashMap<>();
        boolean podeOrdens = temAcesso(session, PermissaoModulo.ORDENS);
        boolean podeEstoque =
                temAcesso(session, PermissaoModulo.PECAS)
                        || temAcesso(session, PermissaoModulo.REPOSICAO)
                        || temAcesso(session, PermissaoModulo.RELATORIO_MENSAL);
        resposta.put("pecas", podeEstoque ? pecasService.listar("").size() : 0);
        resposta.put("ordensAbertas", podeOrdens ? ordensService.ordensAbertas() : 0);
        resposta.put("ordensConcluidasMes", podeOrdens ? ordensService.ordensConcluidasNoMes() : 0);
        resposta.put("criticas", podeEstoque ? pecasService.criticas().size() : 0);
        resposta.put("porStatus", podeOrdens ? ordensService.resumoOrdensPorStatus() : Map.of());
        resposta.put(
                "aguardandoPeca",
                podeOrdens
                        ? ordensService.ordensAguardandoPeca().stream()
                                .map(ApiMapper::ordemResumo)
                                .toList()
                        : List.of());
        resposta.put(
                "retornos",
                podeOrdens
                        ? ordensService.veiculosEmRetorno().stream()
                                .map(ApiMapper::ordemResumo)
                                .toList()
                        : List.of());
        resposta.put(
                "pecasMaisUsadas",
                podeEstoque
                        ? pecasService.pecasMaisUsadas().stream().map(ApiMapper::pecaDto).toList()
                        : List.of());
        if (autorizacao.ehAdministrador(session))
            resposta.put("valorEstoque", pecasService.valorEstoque());
        return ResponseEntity.ok(resposta);
    }
}
