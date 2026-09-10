package br.edu.fag.sputnik.controller.api.dto;

import java.math.BigDecimal;

public record PecaDto(
        Long id,
        String codigo,
        String nome,
        String fabricante,
        String categoria,
        BigDecimal preco,
        Integer saldo,
        Integer disponivel,
        Integer minimo,
        String locacao) {}
