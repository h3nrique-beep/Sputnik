package br.edu.fag.sputnik.controller.api.dto;

import java.util.List;

public record UsuarioSistemaDto(
        Long id,
        String nome,
        String login,
        String perfil,
        List<String> permissoes,
        boolean trocaSenhaObrigatoria) {}
