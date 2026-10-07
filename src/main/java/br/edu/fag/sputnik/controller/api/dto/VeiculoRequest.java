package br.edu.fag.sputnik.controller.api.dto;

public record VeiculoRequest(
        @jakarta.validation.constraints.NotBlank(message = "Informe a placa do veículo.")
                String placa,
        @jakarta.validation.constraints.NotBlank(message = "Informe o modelo do veículo.")
                String marcaModelo,
        @jakarta.validation.constraints.NotNull(message = "Informe o ano do veículo.")
        @jakarta.validation.constraints.Min(value = 1886, message = "Informe um ano válido.")
        @jakarta.validation.constraints.Max(value = 2100, message = "Informe um ano válido.") Integer ano,
        @jakarta.validation.constraints.NotBlank(message = "Informe o chassi do veículo.")
                String chassi,
        @jakarta.validation.constraints.NotNull(message = "Informe a quilometragem atual.")
        @jakarta.validation.constraints.PositiveOrZero(message = "A quilometragem não pode ser negativa.")
                Integer quilometragemAtual,
        Long clienteId) {}
