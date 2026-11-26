package com.cinemel.reserva;

import java.util.UUID;

import com.cinemel.sala.Assento;
import com.cinemel.sessao.Sessao;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "reserva", uniqueConstraints = @UniqueConstraint(
        name = "uk_reserva_sessao_assento", columnNames = {"sessao_id", "assento_id"}))
class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessao_id", nullable = false)
    private Sessao sessao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assento_id", nullable = false)
    private Assento assento;

    protected Reserva() {
    }

    Reserva(Sessao sessao, Assento assento) {
        this.sessao = sessao;
        this.assento = assento;
    }

    UUID getCodigo() {
        return codigo;
    }
}
