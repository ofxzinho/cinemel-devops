package com.cinemel.sessao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cinemel.catalogo.Filme;
import com.cinemel.catalogo.FilmeRepository;
import com.cinemel.catalogo.StatusFilme;
import com.cinemel.sala.Sala;
import com.cinemel.sala.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SessaoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilmeRepository filmeRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private SessaoRepository sessaoRepository;

    private Long filmeId;
    private Long salaId;

    @BeforeEach
    void cadastrarFilmeESala() {
        Filme filme = new Filme();
        filme.setTitulo("Filme da sessão");
        filme.setStatus(StatusFilme.EM_CARTAZ);
        filmeId = filmeRepository.save(filme).getId();
        salaId = salaRepository.save(new Sala(null, "Sala 1", 10, 15)).getId();
    }

    @Test
    void criarComFilmeESalaExistentesRetorna201EPersisteSessao() throws Exception {
        criarSessao(filmeId, salaId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(header().string("Location", containsString("/sessoes/")))
                .andExpect(jsonPath("$.filme.id").value(filmeId))
                .andExpect(jsonPath("$.sala.id").value(salaId))
                .andExpect(jsonPath("$.formato").value("DOIS_D"))
                .andExpect(jsonPath("$.audio").value("LEGENDADO"));

        Sessao sessao = sessaoRepository.findAll().stream()
                .filter(item -> item.getSala().getId().equals(salaId))
                .findFirst().orElseThrow();
        assertThat(sessao.getFilme().getId()).isEqualTo(filmeId);
        assertThat(sessao.getPrecoBase()).isEqualByComparingTo("32.00");
    }

    @Test
    void criarComFilmeInexistenteRetorna404SemPersistir() throws Exception {
        long sessoesAntes = sessaoRepository.count();

        criarSessao(-1L, salaId)
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("Location"));

        assertThat(sessaoRepository.count()).isEqualTo(sessoesAntes);
    }

    @Test
    void criarComSalaInexistenteRetorna404SemPersistir() throws Exception {
        long sessoesAntes = sessaoRepository.count();

        criarSessao(filmeId, -1L)
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("Location"));

        assertThat(sessaoRepository.count()).isEqualTo(sessoesAntes);
    }

    private ResultActions criarSessao(Long filme, Long sala) throws Exception {
        return mockMvc.perform(post("/sessoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "filmeId": %d,
                          "salaId": %d,
                          "dataHora": "2026-12-04T20:30:00",
                          "formato": "DOIS_D",
                          "audio": "LEGENDADO",
                          "precoBase": 32.00
                        }
                        """.formatted(filme, sala)));
    }
}
