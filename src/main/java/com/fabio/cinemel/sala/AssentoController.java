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
@RequestMapping("/assentos")
@Tag(name = "Assentos", description = "Operações de cadastro de assentos")
public class AssentoController {

	private final AssentoRepository assentoRepository;

	public AssentoController(AssentoRepository assentoRepository) {
		this.assentoRepository = assentoRepository;
	}

	// Cria um novo assento e retorna a localização do recurso criado.
	@PostMapping
	public ResponseEntity<Assento> criar(@RequestBody Assento assento) {
		Assento assentoSalvo = assentoRepository.save(assento);
		URI localizacao = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(assentoSalvo.getId())
				.toUri();

		return ResponseEntity.created(localizacao).body(assentoSalvo);
	}

	// Lista todos os assentos cadastrados.
	@GetMapping
	public ResponseEntity<List<Assento>> listar() {
		return ResponseEntity.ok(assentoRepository.findAll());
	}

	// Busca um assento pelo identificador.
	@GetMapping("/{id}")
	public ResponseEntity<Assento> buscarPorId(@PathVariable Long id) {
		return assentoRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Atualiza os dados de um assento existente.
	@PutMapping("/{id}")
	public ResponseEntity<Assento> atualizar(@PathVariable Long id, @RequestBody Assento assentoAtualizado) {
		return assentoRepository.findById(id)
				.map(assento -> {
					assento.setSala(assentoAtualizado.getSala());
					assento.setFileira(assentoAtualizado.getFileira());
					assento.setNumero(assentoAtualizado.getNumero());
					assento.setTipo(assentoAtualizado.getTipo());
					return ResponseEntity.ok(assentoRepository.save(assento));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Remove um assento existente.
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		if (!assentoRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}

		assentoRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
