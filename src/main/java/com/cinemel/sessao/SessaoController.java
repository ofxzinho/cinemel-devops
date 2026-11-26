package com.cinemel.sessao;

import java.net.URI;
import java.util.List;

import com.cinemel.catalogo.Filme;
import com.cinemel.catalogo.FilmeRepository;
import com.cinemel.reserva.ReservaRepository;
import com.cinemel.sala.Sala;
import com.cinemel.sala.SalaRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/sessoes")
@Tag(name = "Sessões", description = "Operações de cadastro de sessões")
public class SessaoController {

	private final SessaoRepository sessaoRepository;
	private final FilmeRepository filmeRepository;
	private final SalaRepository salaRepository;
	private final ReservaRepository reservaRepository;

	public SessaoController(SessaoRepository sessaoRepository, FilmeRepository filmeRepository,
			SalaRepository salaRepository, ReservaRepository reservaRepository) {
		this.sessaoRepository = sessaoRepository;
		this.filmeRepository = filmeRepository;
		this.salaRepository = salaRepository;
		this.reservaRepository = reservaRepository;
	}

	// Cria uma nova sessão e retorna a localização do recurso criado.
	@Operation(summary = "Cadastrar sessão", description = """
			Consulte [GET /filmes](../filmes) e [GET /salas](../salas).
			Copie o campo `id` do filme para `filmeId` e o da sala para `salaId`.
			Use uma data e hora futuras para a sessão aparecer na programação do site.
			""", responses = {
			@ApiResponse(responseCode = "201", description = "Sessão cadastrada"),
			@ApiResponse(responseCode = "404", description = "Filme ou sala não encontrado. Confira filmeId e salaId.",
					content = @Content)
	})
	@PostMapping
	public ResponseEntity<Sessao> criar(@RequestBody SessaoRequest request) {
		return filmeRepository.findById(request.filmeId())
				.flatMap(filme -> salaRepository.findById(request.salaId())
						.map(sala -> salvarNovaSessao(request, filme, sala)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private ResponseEntity<Sessao> salvarNovaSessao(SessaoRequest request,
			Filme filme, Sala sala) {
		Sessao sessao = new Sessao(null, filme, sala, request.dataHora(), request.formato(),
				request.audio(), request.precoBase());
		Sessao sessaoSalva = sessaoRepository.save(sessao);
		URI localizacao = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(sessaoSalva.getId())
				.toUri();

		return ResponseEntity.created(localizacao).body(sessaoSalva);
	}

	// Lista todas as sessões cadastradas.
	@Operation(summary = "Listar sessões")
	@GetMapping
	public ResponseEntity<List<Sessao>> listar() {
		return ResponseEntity.ok(sessaoRepository.findAll());
	}

	// Busca uma sessão pelo identificador.
	@Operation(summary = "Consultar sessão por ID")
	@ApiResponse(responseCode = "200", description = "Sessão encontrada")
	@ApiResponse(responseCode = "404", description = "Sessão não encontrada", content = @Content)
	@GetMapping("/{id}")
	public ResponseEntity<Sessao> buscarPorId(@PathVariable Long id) {
		return sessaoRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Atualiza os dados de uma sessão existente.
	@Operation(summary = "Atualizar sessão")
	@ApiResponse(responseCode = "200", description = "Sessão atualizada")
	@ApiResponse(responseCode = "404", description = "Sessão, filme ou sala não encontrado", content = @Content)
	@ApiResponse(responseCode = "409", description = "Sessão tem reservas", content = @Content)
	@PutMapping("/{id}")
	@Transactional
	public ResponseEntity<Sessao> atualizar(@PathVariable Long id, @RequestBody SessaoRequest request) {
		return sessaoRepository.buscarParaAlteracao(id)
				.map(sessao -> atualizarSessao(sessao, request))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private ResponseEntity<Sessao> atualizarSessao(Sessao sessao, SessaoRequest request) {
		if (reservaRepository.existsBySessaoId(sessao.getId())) {
			return ResponseEntity.status(409).build();
		}
		return filmeRepository.findById(request.filmeId())
				.flatMap(filme -> salaRepository.findById(request.salaId())
						.map(sala -> {
							sessao.setFilme(filme);
							sessao.setSala(sala);
							sessao.setDataHora(request.dataHora());
							sessao.setFormato(request.formato());
							sessao.setAudio(request.audio());
							sessao.setPrecoBase(request.precoBase());
							return ResponseEntity.ok(sessaoRepository.save(sessao));
						}))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Remove uma sessão existente.
	@Operation(summary = "Excluir sessão")
	@ApiResponse(responseCode = "204", description = "Sessão excluída", content = @Content)
	@ApiResponse(responseCode = "404", description = "Sessão não encontrada", content = @Content)
	@ApiResponse(responseCode = "409", description = "Sessão tem reservas", content = @Content)
	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		if (sessaoRepository.buscarParaAlteracao(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		if (reservaRepository.existsBySessaoId(id)) {
			return ResponseEntity.status(409).build();
		}

		sessaoRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
