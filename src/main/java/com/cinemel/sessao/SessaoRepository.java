package com.cinemel.sessao;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {

	List<Sessao> findByFilmeIdOrderByDataHoraAsc(Long filmeId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Sessao s where s.id = :id")
	Optional<Sessao> buscarParaAlteracao(Long id);
}
