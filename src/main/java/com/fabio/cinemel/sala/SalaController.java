package com.fabio.cinemel.sala;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/salas")
@Tag(name = "Salas", description = "Operações de cadastro de salas")
public class SalaController {

	private final SalaRepository salaRepository;

	public SalaController(SalaRepository salaRepository) {
		this.salaRepository = salaRepository;
	}

	// Cria uma nova sala e retorna a localização do recurso criado.
	@PostMapping
	public ResponseEntity<Sala> criar(@RequestBody Sala sala) {
		Sala salaSalva = salaRepository.save(sala);
		URI localizacao = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(salaSalva.getId())
				.toUri();

		return ResponseEntity.created(localizacao).body(salaSalva);
	}

	// Lista todas as salas cadastradas.
	@GetMapping
	public ResponseEntity<List<Sala>> listar() {
		return ResponseEntity.ok(salaRepository.findAll());
	}

	// Busca uma sala pelo identificador.
	@GetMapping("/{id}")
	public ResponseEntity<Sala> buscarPorId(@PathVariable Long id) {
		return salaRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Atualiza os dados de uma sala existente.
	@PutMapping("/{id}")
	public ResponseEntity<Sala> atualizar(@PathVariable Long id, @RequestBody Sala salaAtualizada) {
		return salaRepository.findById(id)
				.map(sala -> {
					sala.setNome(salaAtualizada.getNome());
					sala.setTotalFileiras(salaAtualizada.getTotalFileiras());
					sala.setAssentosPorFileira(salaAtualizada.getAssentosPorFileira());
					return ResponseEntity.ok(salaRepository.save(sala));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Remove uma sala existente.
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		if (!salaRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}

		salaRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
