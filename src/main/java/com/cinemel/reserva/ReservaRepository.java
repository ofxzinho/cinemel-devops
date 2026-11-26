package com.cinemel.reserva;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    boolean existsBySessaoIdAndAssentoId(Long sessaoId, Long assentoId);

    boolean existsBySessaoId(Long sessaoId);

    boolean existsByAssentoId(Long assentoId);

    @Query("select r.assento.id from Reserva r where r.sessao.id = :sessaoId")
    Set<Long> buscarAssentosReservados(Long sessaoId);
}
