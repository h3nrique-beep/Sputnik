package br.edu.fag.sputnik.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record UsuarioDto(
        Long id,
        @Schema(nullable = true) Long empresaId,
        @Schema(nullable = true) String empresaNome,
        String nome,
        String perfil,
        List<String> permissoes,
        boolean trocaSenhaObrigatoria) {}
