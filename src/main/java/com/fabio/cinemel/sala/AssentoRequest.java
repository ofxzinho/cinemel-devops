package com.fabio.cinemel.sala;

import io.swagger.v3.oas.annotations.media.Schema;

public record AssentoRequest(
		@Schema(example = "1") Long salaId,
		@Schema(example = "A") String fileira,
		@Schema(example = "1") Integer numero,
		TipoAssento tipo) {
}
