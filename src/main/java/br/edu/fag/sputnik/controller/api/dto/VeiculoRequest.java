package br.edu.fag.sputnik.controller.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record VeiculoRequest(
        @NotBlank(message = "Informe a placa do veículo.") String placa,
        @NotBlank(message = "Informe o modelo do veículo.") String marcaModelo,
        @NotNull(message = "Informe o ano do veículo.")
                @Min(value = 1886, message = "Informe um ano válido.")
                @Max(value = 2100, message = "Informe um ano válido.")
                Integer ano,
        @NotBlank(message = "Informe o chassi do veículo.") String chassi,
        @NotNull(message = "Informe a quilometragem atual.")
                @PositiveOrZero(message = "A quilometragem não pode ser negativa.")
                Integer quilometragemAtual,
        Long clienteId) {}
