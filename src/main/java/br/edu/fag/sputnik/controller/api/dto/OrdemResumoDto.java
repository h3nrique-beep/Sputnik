package br.edu.fag.sputnik.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrdemResumoDto(
        Long id,
        String status,
        LocalDateTime abertura,
        @Schema(nullable = true) LocalDateTime agendamento,
        @Schema(nullable = true) LocalDate retornoEm,
        @Schema(nullable = true) LocalDate garantiaAte,
        Integer quilometragem,
        String descricao,
        ClienteDto cliente,
        VeiculoDto veiculo,
        @Schema(nullable = true) br.edu.fag.sputnik.domain.DadosDocumentoOs documento) {}
