package br.edu.fag.sputnik.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MovimentacaoDto(
        Long id,
        PecaDto peca,
        String tipo,
        Integer quantidade,
        Integer saldoResultante,
        LocalDateTime data,
        String operador,
        @Schema(nullable = true) String observacao) {}
