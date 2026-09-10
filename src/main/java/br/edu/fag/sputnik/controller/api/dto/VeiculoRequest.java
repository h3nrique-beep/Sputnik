package br.edu.fag.sputnik.controller.api.dto;

public record VeiculoRequest(
        @jakarta.validation.constraints.NotBlank(message = "Informe a placa do veículo.")
                String placa,
        @jakarta.validation.constraints.NotBlank(message = "Informe o modelo do veículo.")
                String marcaModelo,
        @jakarta.validation.constraints.NotNull(message = "Informe o ano do veículo.") Integer ano,
        @jakarta.validation.constraints.NotBlank(message = "Informe o chassi do veículo.")
                String chassi,
        @jakarta.validation.constraints.NotNull(message = "Informe a quilometragem atual.")
                Integer quilometragemAtual,
        Long clienteId) {}
