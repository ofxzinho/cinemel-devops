package com.fabio.cinemel.catalogo;

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
@RequestMapping("/filmes")
@Tag(name = "Filmes", description = "Operações de cadastro de filmes")
public class FilmeController {

	private final FilmeRepository filmeRepository;

	public FilmeController(FilmeRepository filmeRepository) {
		this.filmeRepository = filmeRepository;
	}

	// Cria um novo filme e retorna a localização do recurso criado.
	@PostMapping
	public ResponseEntity<Filme> criar(@RequestBody Filme filme) {
		Filme filmeSalvo = filmeRepository.save(filme);
		URI localizacao = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(filmeSalvo.getId())
				.toUri();

		return ResponseEntity.created(localizacao).body(filmeSalvo);
	}

	// Lista todos os filmes cadastrados.
	@GetMapping
	public ResponseEntity<List<Filme>> listar() {
		return ResponseEntity.ok(filmeRepository.findAll());
	}

	// Busca um filme pelo identificador.
	@GetMapping("/{id}")
	public ResponseEntity<Filme> buscarPorId(@PathVariable Long id) {
		return filmeRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Atualiza os dados de um filme existente.
	@PutMapping("/{id}")
	public ResponseEntity<Filme> atualizar(@PathVariable Long id, @RequestBody Filme filmeAtualizado) {
		return filmeRepository.findById(id)
				.map(filme -> {
					filme.setTitulo(filmeAtualizado.getTitulo());
					filme.setSinopse(filmeAtualizado.getSinopse());
					filme.setDuracaoMinutos(filmeAtualizado.getDuracaoMinutos());
					filme.setClassificacao(filmeAtualizado.getClassificacao());
					filme.setDataEstreia(filmeAtualizado.getDataEstreia());
					filme.setPosterUrl(filmeAtualizado.getPosterUrl());
					filme.setStatus(filmeAtualizado.getStatus());
					return ResponseEntity.ok(filmeRepository.save(filme));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Remove um filme existente.
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		if (!filmeRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}

		filmeRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
