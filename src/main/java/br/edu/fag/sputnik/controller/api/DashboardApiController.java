package br.edu.fag.sputnik.controller.api;

import br.edu.fag.sputnik.controller.api.mapper.ApiMapper;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.EstoqueService;

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
    private final EstoqueService estoque;

    public DashboardApiController(AutorizacaoService autorizacao, EstoqueService estoque) {
        super(autorizacao);
        this.estoque = estoque;
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
        resposta.put("pecas", podeEstoque ? estoque.listar("").size() : 0);
        resposta.put("ordensAbertas", podeOrdens ? estoque.ordensAbertas() : 0);
        resposta.put("ordensConcluidasMes", podeOrdens ? estoque.ordensConcluidasNoMes() : 0);
        resposta.put("criticas", podeEstoque ? estoque.criticas().size() : 0);
        resposta.put("porStatus", podeOrdens ? estoque.resumoOrdensPorStatus() : Map.of());
        resposta.put(
                "aguardandoPeca",
                podeOrdens
                        ? estoque.ordensAguardandoPeca().stream()
                                .map(ApiMapper::ordemResumo)
                                .toList()
                        : List.of());
        resposta.put(
                "retornos",
                podeOrdens
                        ? estoque.veiculosEmRetorno().stream().map(ApiMapper::ordemResumo).toList()
                        : List.of());
        resposta.put(
                "pecasMaisUsadas",
                podeEstoque
                        ? estoque.pecasMaisUsadas().stream().map(ApiMapper::pecaDto).toList()
                        : List.of());
        if (autorizacao.ehAdministrador(session))
            resposta.put("valorEstoque", estoque.valorEstoque());
        return ResponseEntity.ok(resposta);
    }
}
