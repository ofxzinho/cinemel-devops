package com.cinemel.sala;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/assentos")
@Tag(name = "Assentos", description = "Operações de cadastro de assentos")
public class AssentoController {

	private final AssentoRepository assentoRepository;
	private final SalaRepository salaRepository;

	public AssentoController(AssentoRepository assentoRepository, SalaRepository salaRepository) {
		this.assentoRepository = assentoRepository;
		this.salaRepository = salaRepository;
	}

	// Cria um novo assento e retorna a localização do recurso criado.
	@Operation(summary = "Cadastrar assento", responses = {
			@ApiResponse(responseCode = "201", description = "Assento cadastrado"),
			@ApiResponse(responseCode = "404", description = "Sala inexistente", content = @Content),
			@ApiResponse(responseCode = "409", description = "Já existe um assento nessa sala, fileira e número",
					content = @Content(mediaType = "application/problem+json",
							schema = @Schema(implementation = ProblemDetail.class)))
	})
	@PostMapping
	public ResponseEntity<Assento> criar(@RequestBody AssentoRequest request) {
		return salaRepository.findById(request.salaId())
				.map(sala -> salvarNovoAssento(request, sala))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private ResponseEntity<Assento> salvarNovoAssento(AssentoRequest request, Sala sala) {
		Assento assento = new Assento(null, sala, request.fileira(), request.numero(), request.tipo());
		Assento assentoSalvo = assentoRepository.save(assento);
		URI localizacao = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(assentoSalvo.getId())
				.toUri();

		return ResponseEntity.created(localizacao).body(assentoSalvo);
	}

	// Lista todos os assentos cadastrados.
	@Operation(summary = "Listar assentos")
	@GetMapping
	public ResponseEntity<List<Assento>> listar() {
		return ResponseEntity.ok(assentoRepository.findAll());
	}

	// Busca um assento pelo identificador.
	@Operation(summary = "Consultar assento por ID")
	@GetMapping("/{id}")
	public ResponseEntity<Assento> buscarPorId(@PathVariable Long id) {
		return assentoRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Atualiza os dados de um assento existente.
	@Operation(summary = "Atualizar assento")
	@PutMapping("/{id}")
	public ResponseEntity<Assento> atualizar(@PathVariable Long id, @RequestBody AssentoRequest request) {
		return assentoRepository.findById(id)
				.map(assento -> atualizarAssento(assento, request))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private ResponseEntity<Assento> atualizarAssento(Assento assento, AssentoRequest request) {
		return salaRepository.findById(request.salaId())
				.map(sala -> {
					assento.setSala(sala);
					assento.setFileira(request.fileira());
					assento.setNumero(request.numero());
					assento.setTipo(request.tipo());
					return ResponseEntity.ok(assentoRepository.save(assento));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail tratarConflitoDeAssento(DataIntegrityViolationException erro) {
		if (erro.getCause() instanceof ConstraintViolationException violacao
				&& "23505".equals(violacao.getSQLState())
				&& identificaAssentoDuplicado(violacao)) {
			return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
					"Já existe um assento com essa fileira e número na sala selecionada.");
		}
		throw erro;
	}

	private boolean identificaAssentoDuplicado(ConstraintViolationException violacao) {
		String nome = violacao.getConstraintName();
		if (nome == null) {
			// O H2 pode informar o nome da restrição apenas na mensagem do driver.
			nome = violacao.getSQLException().getMessage();
		}
		return nome.toLowerCase(Locale.ROOT).contains("uk_assento_sala_fileira_numero");
	}

	// Remove um assento existente.
	@Operation(summary = "Excluir assento")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		if (!assentoRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}

		assentoRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
