package br.edu.fag.sputnik.controller.api;

import br.edu.fag.sputnik.controller.api.dto.DashboardDto;
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
    public ResponseEntity<DashboardDto> dashboard(HttpSession session) {
        if (!autorizacao.estaLogado(session)) return naoAutorizado();
        if (!temAcesso(session, PermissaoModulo.PECAS)
                && !temAcesso(session, PermissaoModulo.ORDENS)
                && !temAcesso(session, PermissaoModulo.RELATORIO_MENSAL)
                && !temAcesso(session, PermissaoModulo.REPOSICAO)) return proibido();
        boolean podeOrdens = temAcesso(session, PermissaoModulo.ORDENS);
        boolean podeEstoque =
                temAcesso(session, PermissaoModulo.PECAS)
                        || temAcesso(session, PermissaoModulo.REPOSICAO)
                        || temAcesso(session, PermissaoModulo.RELATORIO_MENSAL);
        return ResponseEntity.ok(
                new DashboardDto(
                        podeEstoque ? pecasService.listar("").size() : 0,
                        podeOrdens ? ordensService.ordensAbertas() : 0,
                        podeOrdens ? ordensService.ordensConcluidasNoMes() : 0,
                        podeEstoque ? pecasService.criticas().size() : 0,
                        podeOrdens ? ordensService.resumoOrdensPorStatus() : Map.of(),
                        podeOrdens
                                ? ordensService.ordensAguardandoPeca().stream()
                                        .map(ApiMapper::ordemResumo)
                                        .toList()
                                : List.of(),
                        podeOrdens
                                ? ordensService.veiculosEmRetorno().stream()
                                        .map(ApiMapper::ordemResumo)
                                        .toList()
                                : List.of(),
                        podeEstoque
                                ? pecasService.pecasMaisUsadas().stream()
                                        .map(ApiMapper::pecaDto)
                                        .toList()
                                : List.of(),
                        autorizacao.ehAdministrador(session) ? pecasService.valorEstoque() : null));
    }
}
