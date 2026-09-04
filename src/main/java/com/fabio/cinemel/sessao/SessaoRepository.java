package com.fabio.cinemel.sessao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {

	List<Sessao> findByFilmeIdOrderByDataHoraAsc(Long filmeId);
}
