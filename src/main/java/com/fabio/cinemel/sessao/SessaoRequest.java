package com.fabio.cinemel.sessao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record SessaoRequest(
		@Schema(example = "1") Long filmeId,
		@Schema(example = "1") Long salaId,
		@Schema(example = "2026-09-10T20:30:00") LocalDateTime dataHora,
		Formato formato,
		Audio audio,
		@Schema(example = "32.00") BigDecimal precoBase) {
}
