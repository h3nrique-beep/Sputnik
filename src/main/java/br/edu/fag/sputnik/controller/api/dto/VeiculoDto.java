package br.edu.fag.sputnik.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record VeiculoDto(
        Long id,
        String placa,
        String marcaModelo,
        Integer ano,
        String chassi,
        Integer quilometragemAtual,
        @Schema(nullable = true) ClienteDto cliente) {}
