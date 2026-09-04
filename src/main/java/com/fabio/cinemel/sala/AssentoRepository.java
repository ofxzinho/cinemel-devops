package com.fabio.cinemel.sala;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssentoRepository extends JpaRepository<Assento, Long> {

	List<Assento> findBySalaIdOrderByFileiraAscNumeroAsc(Long salaId);
}
