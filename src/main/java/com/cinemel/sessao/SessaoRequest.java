package com.cinemel.sessao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record SessaoRequest(
		@Schema(description = "Identificador do filme", example = "1") Long filmeId,
		@Schema(description = "Identificador da sala", example = "1") Long salaId,
		@Schema(example = "2026-12-04T20:30:00") LocalDateTime dataHora,
		Formato formato,
		Audio audio,
		@Schema(example = "32.00") BigDecimal precoBase) {
}
