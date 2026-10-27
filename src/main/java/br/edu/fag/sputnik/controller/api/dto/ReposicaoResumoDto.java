package br.edu.fag.sputnik.controller.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReposicaoResumoDto(List<ReposicaoDto> pecas, BigDecimal custo, long zeradas) {}
