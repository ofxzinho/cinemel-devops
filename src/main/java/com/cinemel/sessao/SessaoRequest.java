package com.cinemel.sessao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record SessaoRequest(
		@Schema(description = "Copie o campo id do filme retornado por GET /filmes.", example = "1") Long filmeId,
		@Schema(description = "Copie o campo id da sala retornado por GET /salas.", example = "1") Long salaId,
		@Schema(description = "Use uma data futura para exibir a sessão na programação.",
				example = "2026-12-04T20:30:00") LocalDateTime dataHora,
		Formato formato,
		Audio audio,
		@Schema(example = "32.00") BigDecimal precoBase) {
}
