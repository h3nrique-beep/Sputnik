package br.edu.fag.sputnik.controller.api;

import br.edu.fag.sputnik.controller.api.mapper.ApiMapper;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.EstoqueService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
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
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("pecas", estoque.listar("").size());
        resposta.put("ordensAbertas", estoque.ordensAbertas());
        resposta.put("ordensConcluidasMes", estoque.ordensConcluidasNoMes());
        resposta.put("criticas", estoque.criticas().size());
        resposta.put("porStatus", estoque.resumoOrdensPorStatus());
        resposta.put(
                "aguardandoPeca",
                estoque.ordensAguardandoPeca().stream().map(ApiMapper::ordemResumo).toList());
        resposta.put(
                "retornos",
                estoque.veiculosEmRetorno().stream().map(ApiMapper::ordemResumo).toList());
        resposta.put(
                "pecasMaisUsadas",
                estoque.pecasMaisUsadas().stream().map(ApiMapper::pecaDto).toList());
        if (autorizacao.ehAdministrador(session))
            resposta.put("valorEstoque", estoque.valorEstoque());
        return ResponseEntity.ok(resposta);
    }
}
