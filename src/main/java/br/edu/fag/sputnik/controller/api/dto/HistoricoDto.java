package br.edu.fag.sputnik.controller.api.dto;

import java.time.LocalDateTime;

public record HistoricoDto(LocalDateTime data, String responsavel, String descricao) {}
