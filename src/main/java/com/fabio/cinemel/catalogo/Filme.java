package com.fabio.cinemel.catalogo;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "filme")
public class Filme {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@Column(nullable = false, length = 150)
	@Schema(example = "Homem-Aranha: Sem Volta pra Casa")
	private String titulo;

	@Column(length = 2000)
	@Schema(example = "Peter Parker tem sua identidade revelada ao mundo.")
	private String sinopse;

	@Schema(example = "148")
	private Integer duracaoMinutos;

	@Column(length = 3)
	@Schema(example = "12")
	private String classificacao;

	@Schema(example = "2026-09-10")
	private LocalDate dataEstreia;

	@Schema(example = "https://image.tmdb.org/t/p/w500/exemplo.jpg")
	private String posterUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusFilme status;
}
