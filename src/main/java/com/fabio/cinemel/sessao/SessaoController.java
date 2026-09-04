package com.fabio.cinemel.sessao;

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
@RequestMapping("/sessoes")
@Tag(name = "Sessoes", description = "Operações de cadastro de sessões")
public class SessaoController {

	private final SessaoRepository sessaoRepository;

	public SessaoController(SessaoRepository sessaoRepository) {
		this.sessaoRepository = sessaoRepository;
	}

	// Cria uma nova sessão e retorna a localização do recurso criado.
	@PostMapping
	public ResponseEntity<Sessao> criar(@RequestBody Sessao sessao) {
		Sessao sessaoSalva = sessaoRepository.save(sessao);
		URI localizacao = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(sessaoSalva.getId())
				.toUri();

		return ResponseEntity.created(localizacao).body(sessaoSalva);
	}

	// Lista todas as sessões cadastradas.
	@GetMapping
	public ResponseEntity<List<Sessao>> listar() {
		return ResponseEntity.ok(sessaoRepository.findAll());
	}

	// Busca uma sessão pelo identificador.
	@GetMapping("/{id}")
	public ResponseEntity<Sessao> buscarPorId(@PathVariable Long id) {
		return sessaoRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Atualiza os dados de uma sessão existente.
	@PutMapping("/{id}")
	public ResponseEntity<Sessao> atualizar(@PathVariable Long id, @RequestBody Sessao sessaoAtualizada) {
		return sessaoRepository.findById(id)
				.map(sessao -> {
					sessao.setFilme(sessaoAtualizada.getFilme());
					sessao.setSala(sessaoAtualizada.getSala());
					sessao.setDataHora(sessaoAtualizada.getDataHora());
					sessao.setFormato(sessaoAtualizada.getFormato());
					sessao.setAudio(sessaoAtualizada.getAudio());
					sessao.setPrecoBase(sessaoAtualizada.getPrecoBase());
					return ResponseEntity.ok(sessaoRepository.save(sessao));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Remove uma sessão existente.
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		if (!sessaoRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}

		sessaoRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
