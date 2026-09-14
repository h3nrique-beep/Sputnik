package br.edu.fag.sputnik.controller.api.dto;

import java.time.LocalDateTime;

public record OrdemResumoDto(
        Long id,
        String status,
        LocalDateTime abertura,
        LocalDateTime agendamento,
        java.time.LocalDate retornoEm,
        java.time.LocalDate garantiaAte,
        Integer quilometragem,
        String descricao,
        ClienteDto cliente,
        VeiculoDto veiculo,
        br.edu.fag.sputnik.domain.DadosDocumentoOs documento) {}
