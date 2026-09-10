package br.edu.fag.sputnik.controller.api.dto;

import java.util.List;

public record OrdemDetalheDto(
        OrdemResumoDto ordem, List<ItemDto> itens, List<HistoricoDto> historico) {}
