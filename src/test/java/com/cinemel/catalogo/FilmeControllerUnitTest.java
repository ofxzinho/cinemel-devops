package com.cinemel.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FilmeControllerUnitTest {

    @Mock
    private FilmeRepository filmeRepository;

    @InjectMocks
    private FilmeController filmeController;

    @Test
    void buscarPorId_quandoFilmeNaoExiste_retorna404() {
        // Arrange (preparar)
        when(filmeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act (executar)
        ResponseEntity<Filme> resposta = filmeController.buscarPorId(99L);

        // Assert (verificar)
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void excluir_quandoFilmeExiste_chamaDeleteERetorna204() {
        when(filmeRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<Void> resposta = filmeController.excluir(1L);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(filmeRepository).deleteById(1L);
    }
}