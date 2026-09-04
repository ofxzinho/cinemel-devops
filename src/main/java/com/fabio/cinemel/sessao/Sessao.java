package com.fabio.cinemel.sessao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fabio.cinemel.catalogo.Filme;
import com.fabio.cinemel.sala.Sala;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "sessao")
public class Sessao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "filme_id")
	@Schema(description = "Informe apenas o id de um filme existente.")
	private Filme filme;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "sala_id")
	@Schema(description = "Informe apenas o id de uma sala existente.")
	private Sala sala;

	@Column(nullable = false)
	@Schema(example = "2026-09-10T20:30:00")
	private LocalDateTime dataHora;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Formato formato;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Audio audio;

	@Column(nullable = false, precision = 10, scale = 2)
	@Schema(example = "32.00")
	private BigDecimal precoBase;
}
