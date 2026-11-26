package com.cinemel.reserva;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cinemel.catalogo.Filme;
import com.cinemel.catalogo.FilmeRepository;
import com.cinemel.catalogo.StatusFilme;
import com.cinemel.sala.Assento;
import com.cinemel.sala.AssentoRepository;
import com.cinemel.sala.Sala;
import com.cinemel.sala.SalaRepository;
import com.cinemel.sala.TipoAssento;
import com.cinemel.sessao.Audio;
import com.cinemel.sessao.Formato;
import com.cinemel.sessao.Sessao;
import com.cinemel.sessao.SessaoRepository;
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
class ReservaApiTest {

    @Autowired private MockMvc api;
    @Autowired private FilmeRepository filmes;
    @Autowired private SalaRepository salas;
    @Autowired private AssentoRepository assentos;
    @Autowired private SessaoRepository sessoes;

    @Test
    void reservaAssentoUmaVezPorSessaoEProtegeOsVinculos() throws Exception {
        Filme filme = new Filme();
        filme.setTitulo("Filme para reserva");
        filme.setStatus(StatusFilme.EM_CARTAZ);
        filmes.save(filme);

        Sala sala = salas.save(new Sala(null, "Sala da reserva", 4, 8));
        Sala outraSala = salas.save(new Sala(null, "Outra sala da reserva", 4, 8));
        Long lugar = assentos.save(new Assento(null, sala, "A", 1, TipoAssento.NORMAL)).getId();
        Long outroLugar = assentos.save(new Assento(null, outraSala, "A", 1, TipoAssento.NORMAL)).getId();
        LocalDateTime horario = LocalDateTime.now().plusDays(2).withNano(0);
        Long sessao = sessoes.save(new Sessao(null, filme, sala, horario,
                Formato.DOIS_D, Audio.LEGENDADO, BigDecimal.TEN)).getId();
        Long outraSessao = sessoes.save(new Sessao(null, filme, sala, horario.plusHours(3),
                Formato.DOIS_D, Audio.LEGENDADO, BigDecimal.TEN)).getId();

        reservar(sessao, outroLugar).andExpect(status().isNotFound());
        reservar(sessao, lugar).andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").isString())
                .andExpect(jsonPath("$.assento").value("A1"));
        reservar(sessao, lugar).andExpect(status().isConflict());
        api.perform(get("/reservas/disponibilidade/{id}", sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservado").value(true));
        reservar(outraSessao, lugar).andExpect(status().isCreated());

        api.perform(delete("/assentos/{id}", lugar)).andExpect(status().isConflict());
        api.perform(delete("/sessoes/{id}", sessao)).andExpect(status().isConflict());
    }

    private ResultActions reservar(Long sessao, Long assento) throws Exception {
        return api.perform(post("/reservas").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sessaoId": %d, "assentoId": %d}
                        """.formatted(sessao, assento)));
    }
}
