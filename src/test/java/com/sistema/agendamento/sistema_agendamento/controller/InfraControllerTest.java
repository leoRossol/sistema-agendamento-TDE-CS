package com.sistema.agendamento.sistema_agendamento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.agendamento.sistema_agendamento.dto.AgendaItemDTO;
import com.sistema.agendamento.sistema_agendamento.dto.SalaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.SalaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.exception.ConflictException;
import com.sistema.agendamento.sistema_agendamento.service.SalaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InfraController.class)
@AutoConfigureMockMvc(addFilters = false)
class InfraControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean SalaService salaService;

    @Test
    void postAlocacoes_deveRetornar201() throws Exception {
        var req = new SalaRequestDTO(1L, 10L,
                LocalDateTime.now().plusDays(1).withHour(10),
                LocalDateTime.now().plusDays(1).withHour(12),
                null);

        when(salaService.alocar(any(SalaRequestDTO.class)))
                .thenReturn(new SalaResponseDTO(123L, false, "Alocação criada"));

        mvc.perform(post("/infra/alocacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reservaId").value(123))
            .andExpect(jsonPath("$.conflito").value(false));
    }

    @Test
    void postAlocacoes_conflitoRetorna409() throws Exception {
        var req = new SalaRequestDTO(1L, 10L,
                LocalDateTime.now().plusDays(1).withHour(10),
                LocalDateTime.now().plusDays(1).withHour(12),
                null);

        when(salaService.alocar(any(SalaRequestDTO.class)))
                .thenThrow(new ConflictException("Sala já alocada neste horário"));

        mvc.perform(post("/infra/alocacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    @Test
    void getAgenda_deveRetornar200() throws Exception {
        var agora = LocalDateTime.now();
        when(salaService.agendaSalaNoMes(10L, java.time.YearMonth.parse("2025-10")))
                .thenReturn(List.of(new AgendaItemDTO(1L, 1L, agora, agora.plusHours(2))));

        mvc.perform(get("/infra/salas/10/agenda")
                .param("periodo", "2025-10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reservaId").value(1))
            .andExpect(jsonPath("$[0].turmaId").value(1));
    }
}
