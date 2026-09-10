package br.edu.fag.sputnik.controller.api.dto;

public record VeiculoDto(
        Long id,
        String placa,
        String marcaModelo,
        Integer ano,
        String chassi,
        Integer quilometragemAtual,
        ClienteDto cliente) {}
