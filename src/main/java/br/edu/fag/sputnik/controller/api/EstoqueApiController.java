package br.edu.fag.sputnik.controller.api;

import static br.edu.fag.sputnik.controller.api.mapper.ApiMapper.pecaDto;

import br.edu.fag.sputnik.controller.api.dto.ReposicaoDto;
import br.edu.fag.sputnik.controller.api.mapper.ApiMapper;
import br.edu.fag.sputnik.domain.Peca;
import br.edu.fag.sputnik.domain.PermissaoModulo;
import br.edu.fag.sputnik.service.AutorizacaoService;
import br.edu.fag.sputnik.service.PecasService;
import br.edu.fag.sputnik.web.MovimentacaoForm;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EstoqueApiController extends ApiSupport {
    private final PecasService pecasService;

    public EstoqueApiController(AutorizacaoService autorizacao, PecasService pecasService) {
        super(autorizacao);
        this.pecasService = pecasService;
    }

    @GetMapping("/pecas")
    public ResponseEntity<?> pecas(
            @RequestParam(required = false) String busca, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.PECAS)) return proibido();
        return ResponseEntity.ok(
                pecasService.listar(busca).stream().map(ApiMapper::pecaDto).toList());
    }

    @PostMapping("/pecas")
    public ResponseEntity<?> salvarPeca(@Valid @RequestBody Peca peca, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.PECAS)) return proibido();
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pecaDto(pecasService.salvar(peca)));
        } catch (IllegalArgumentException exception) {
            return invalido(exception);
        }
    }

    @PostMapping("/movimentacoes")
    public ResponseEntity<?> movimentar(
            @Valid @RequestBody MovimentacaoForm form, HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.PECAS)) return proibido();
        try {
            pecasService.movimentar(form, operador(session));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return invalido(exception);
        }
    }

    @GetMapping("/relatorio-mensal")
    public ResponseEntity<?> relatorioMensal(HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.RELATORIO_MENSAL)) return proibido();
        return ResponseEntity.ok(
                pecasService.relatorioMensal().stream().map(ApiMapper::movimentacaoDto).toList());
    }

    @GetMapping("/reposicao")
    public ResponseEntity<?> reposicao(HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.REPOSICAO)) return proibido();
        var pecas =
                pecasService.criticas().stream()
                        .map(peca -> new ReposicaoDto(pecaDto(peca), pecasService.sugestao(peca)))
                        .toList();
        return ResponseEntity.ok(
                Map.of(
                        "pecas",
                        pecas,
                        "custo",
                        pecasService.custoReposicao(),
                        "zeradas",
                        pecasService.quantidadeZeradas()));
    }

    @GetMapping(value = "/reposicao.csv", produces = "text/csv")
    public ResponseEntity<?> exportarReposicao(HttpSession session) {
        if (!temAcesso(session, PermissaoModulo.REPOSICAO)) return proibido();
        // BOM para o Excel reconhecer UTF-8 e manter os acentos.
        byte[] arquivo =
                ("﻿" + pecasService.gerarCsvReposicao()).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=relatorio-reposicao.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(arquivo);
    }
}
