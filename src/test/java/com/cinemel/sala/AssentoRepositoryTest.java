package com.cinemel.sala;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class AssentoRepositoryTest {

    @Autowired
    private AssentoRepository assentoRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Test
    void deveBuscarAssentosDaSalaEmOrdemDeFileiraENumero() {
        // Arrange
        Sala sala1 = salaRepository.save(new Sala(null, "Sala 1", 10, 15));
        Sala sala2 = salaRepository.save(new Sala(null, "Sala 2", 10, 15));

        assentoRepository.save(new Assento(null, sala1, "B", 2, TipoAssento.NORMAL));
        assentoRepository.save(new Assento(null, sala1, "A", 2, TipoAssento.NORMAL));
        assentoRepository.save(new Assento(null, sala1, "A", 1, TipoAssento.PCD));
        assentoRepository.save(new Assento(null, sala2, "A", 1, TipoAssento.NORMAL));

        // Act
        List<Assento> resultado =
                assentoRepository.findBySalaIdOrderByFileiraAscNumeroAsc(sala1.getId());

     System.out.println("Salvos (fora de ordem): B2, A2, A1 na sala 1 e A1 na sala 2");
        System.out.println("Assentos da sala 1: " + resultado.stream()
                .map(a -> a.getFileira() + a.getNumero())
                .toList());

        // Assert
        assertThat(resultado)
                .extracting(a -> a.getFileira() + a.getNumero())
                .containsExactly("A1", "A2", "B2");
    }
}