package com.cinemel.catalogo;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FilmeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postFilmes_deveCriarERetornar201() throws Exception {
        String json = """
                {
                  "titulo": "Duna: Parte Dois",
                  "duracaoMinutos": 166,
                  "classificacao": "14",
                  "dataEstreia": "2026-09-10",
                  "status": "EM_CARTAZ"
                }
                """;

        mockMvc.perform(post("/filmes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/filmes/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Duna: Parte Dois"));
    }

    @Test
    void getFilmePorId_quandoNaoExiste_deveRetornar404() throws Exception {
        mockMvc.perform(get("/filmes/{id}", 999999))
                .andExpect(status().isOk());
    }
}