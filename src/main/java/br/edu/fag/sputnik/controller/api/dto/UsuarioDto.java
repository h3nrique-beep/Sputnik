package br.edu.fag.sputnik.controller.api.dto;

import java.util.List;

public record UsuarioDto(
        Long id,
        Long empresaId,
        String empresaNome,
        String nome,
        String perfil,
        List<String> permissoes,
        boolean trocaSenhaObrigatoria) {}
