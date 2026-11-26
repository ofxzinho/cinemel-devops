package com.cinemel.reserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.cinemel.sala.Assento;
import com.cinemel.sala.AssentoRepository;
import com.cinemel.sessao.Sessao;
import com.cinemel.sessao.SessaoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservas")
@Tag(name = "Reservas")
class ReservaController {

    private final ReservaRepository reservas;
    private final SessaoRepository sessoes;
    private final AssentoRepository assentos;

    ReservaController(ReservaRepository reservas, SessaoRepository sessoes, AssentoRepository assentos) {
        this.reservas = reservas;
        this.sessoes = sessoes;
        this.assentos = assentos;
    }

    @GetMapping("/disponibilidade/{sessaoId}")
    @Operation(summary = "Consultar lugares disponíveis na sessão")
    @Transactional(readOnly = true)
    ResponseEntity<?> consultarDisponibilidade(@PathVariable Long sessaoId) {
        Sessao sessao = sessoes.findById(sessaoId).orElse(null);
        if (sessao == null) {
            return problema(HttpStatus.NOT_FOUND, "Sessão inexistente.");
        }

        Set<Long> ocupados = reservas.buscarAssentosReservados(sessaoId);
        List<Lugar> lugares = assentos.findBySalaIdOrderByFileiraAscNumeroAsc(sessao.getSala().getId())
                .stream().map(assento -> new Lugar(
                        assento.getId(), assento.getFileira(), assento.getNumero(),
                        ocupados.contains(assento.getId())))
                .toList();
        return ResponseEntity.ok(lugares);
    }

    @PostMapping
    @Operation(summary = "Reservar um lugar sem pagamento")
    @Transactional
    ResponseEntity<?> reservar(@RequestBody Pedido pedido) {
        if (pedido.sessaoId() == null || pedido.assentoId() == null) {
            return problema(HttpStatus.BAD_REQUEST, "Informe a sessão e o assento.");
        }

        Sessao sessao = sessoes.buscarParaAlteracao(pedido.sessaoId()).orElse(null);
        if (sessao == null) {
            return problema(HttpStatus.NOT_FOUND, "Sessão inexistente.");
        }
        Assento assento = assentos.buscarParaAlteracao(pedido.assentoId()).orElse(null);
        if (assento == null || !assento.getSala().getId().equals(sessao.getSala().getId())) {
            return problema(HttpStatus.NOT_FOUND, "Esse assento não pertence à sala da sessão.");
        }
        if (reservas.existsBySessaoIdAndAssentoId(sessao.getId(), assento.getId())) {
            return problema(HttpStatus.CONFLICT, "Esse assento já foi reservado para esta sessão.");
        }

        Reserva reserva = reservas.save(new Reserva(sessao, assento));
        Comprovante comprovante = new Comprovante(reserva.getCodigo(), sessao.getFilme().getTitulo(),
                sessao.getSala().getNome(), sessao.getDataHora(), assento.getFileira() + assento.getNumero());
        return ResponseEntity.status(HttpStatus.CREATED).body(comprovante);
    }

    private ResponseEntity<ProblemDetail> problema(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, mensagem));
    }

    record Pedido(Long sessaoId, Long assentoId) {
    }

    record Lugar(Long id, String fileira, int numero, boolean reservado) {
    }

    record Comprovante(UUID codigo, String filme, String sala, LocalDateTime dataHora, String assento) {
    }
}
