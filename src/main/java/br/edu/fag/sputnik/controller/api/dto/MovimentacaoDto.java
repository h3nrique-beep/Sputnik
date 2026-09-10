package br.edu.fag.sputnik.controller.api.dto;

import java.time.LocalDateTime;

public record MovimentacaoDto(
        Long id,
        PecaDto peca,
        String tipo,
        Integer quantidade,
        Integer saldoResultante,
        LocalDateTime data,
        String operador,
        String observacao) {}
