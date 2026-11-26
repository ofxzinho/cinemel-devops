package com.cinemel.sala;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssentoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private AssentoRepository assentoRepository;

    @Test
    void cadastrarMesmoAssentoRetorna409MasPermiteOutraSala() throws Exception {
        Long sala = salaRepository.save(new Sala(null, "Sala do conflito", 5, 8)).getId();
        Long outraSala = salaRepository.save(new Sala(null, "Sala sem conflito", 5, 8)).getId();

        cadastrar(sala).andExpect(status().isCreated());
        cadastrar(sala)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Já existe um assento com essa fileira e número na sala selecionada."));
        cadastrar(outraSala).andExpect(status().isCreated());

        assertThat(assentoRepository.findBySalaIdOrderByFileiraAscNumeroAsc(sala)).hasSize(1);
    }

    private ResultActions cadastrar(Long salaId) throws Exception {
        return mockMvc.perform(post("/assentos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"salaId": %d, "fileira": "A", "numero": 1, "tipo": "NORMAL"}
                        """.formatted(salaId)));
    }
}
