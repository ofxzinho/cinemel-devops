package com.cinemel.sala;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AssentoRepository extends JpaRepository<Assento, Long> {

	List<Assento> findBySalaIdOrderByFileiraAscNumeroAsc(Long salaId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Assento a where a.id = :id")
	Optional<Assento> buscarParaAlteracao(Long id);
}
