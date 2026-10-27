package br.edu.fag.sputnik.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoDto(
        Long id,
        String tipo,
        String status,
        String descricao,
        @Schema(nullable = true) String categoria,
        BigDecimal valor,
        LocalDate vencimento,
        @Schema(nullable = true) LocalDate pagamentoEm,
        @Schema(nullable = true) Long ordemServicoId) {}
