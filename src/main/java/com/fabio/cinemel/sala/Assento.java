package com.fabio.cinemel.sala;

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
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "assento", uniqueConstraints = @UniqueConstraint(
		name = "uk_assento_sala_fileira_numero",
		columnNames = {"sala_id", "fileira", "numero"}))
public class Assento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "sala_id")
	@Schema(description = "Informe apenas o id de uma sala existente.")
	private Sala sala;

	@Column(nullable = false, length = 2)
	@Schema(example = "A")
	private String fileira;

	@Column(nullable = false)
	@Schema(example = "1")
	private Integer numero;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoAssento tipo;
}
