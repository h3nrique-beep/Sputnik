package br.edu.fag.sputnik.controller.api.dto;

import java.math.BigDecimal;

public record ItemDto(
        Long id,
        String tipo,
        String descricao,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal total) {}
