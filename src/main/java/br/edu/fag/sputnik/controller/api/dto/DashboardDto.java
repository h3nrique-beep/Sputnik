package br.edu.fag.sputnik.controller.api.dto;

import br.edu.fag.sputnik.domain.StatusOrdemServico;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Indicadores do painel; listas vêm vazias para quem não tem o módulo. */
public record DashboardDto(
        int pecas,
        long ordensAbertas,
        long ordensConcluidasMes,
        int criticas,
        Map<StatusOrdemServico, Long> porStatus,
        List<OrdemResumoDto> aguardandoPeca,
        List<OrdemResumoDto> retornos,
        List<PecaDto> pecasMaisUsadas,
        @Schema(nullable = true, description = "Somente para administradores.")
                BigDecimal valorEstoque) {}
