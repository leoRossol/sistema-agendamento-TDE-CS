package com.sistema.agendamento.sistema_agendamento.controller;

import com.sistema.agendamento.sistema_agendamento.dto.RelatorioOcupacaoResponseDTO;
import com.sistema.agendamento.sistema_agendamento.service.ReportsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportsController.class)
class ReportsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportsService reportsService;

    private RelatorioOcupacaoResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        // Criar resposta mock
        mockResponse = new RelatorioOcupacaoResponseDTO();
        mockResponse.setPeriodo("2025/2");

        // Mock ocupação por sala
        RelatorioOcupacaoResponseDTO.OcupacaoPorSalaDTO ocupacaoSala = 
            new RelatorioOcupacaoResponseDTO.OcupacaoPorSalaDTO(
                1L, "Sala A1", "A1", 50, 320, 1280, 
                BigDecimal.valueOf(25.00), Collections.emptyList()
            );
        mockResponse.setOcupacaoPorSala(Arrays.asList(ocupacaoSala));

        // Mock ocupação por curso
        RelatorioOcupacaoResponseDTO.OcupacaoPorCursoDTO ocupacaoCurso = 
            new RelatorioOcupacaoResponseDTO.OcupacaoPorCursoDTO(
                1L, "Ciência da Computação", "CC", 640, 2560, 
                BigDecimal.valueOf(25.00), 2
            );
        mockResponse.setOcupacaoPorCurso(Arrays.asList(ocupacaoCurso));

        // Mock ocupação por disciplina
        RelatorioOcupacaoResponseDTO.OcupacaoPorDisciplinaDTO ocupacaoDisciplina = 
            new RelatorioOcupacaoResponseDTO.OcupacaoPorDisciplinaDTO(
                1L, "Algoritmos", "ALG001", 1L, "Ciência da Computação", 
                320, 1280, BigDecimal.valueOf(25.00), 1
            );
        mockResponse.setOcupacaoPorDisciplina(Arrays.asList(ocupacaoDisciplina));

        // Mock resumo
        RelatorioOcupacaoResponseDTO.ResumoOcupacaoDTO resumo = 
            new RelatorioOcupacaoResponseDTO.ResumoOcupacaoDTO(
                10, 5, BigDecimal.valueOf(30.50), 3, 8, 12, 960, 2560
            );
        mockResponse.setResumo(resumo);
    }

    @Test
    void gerarRelatorioOcupacao_ComPeriodoValido_DeveRetornarRelatorio() throws Exception {
        when(reportsService.gerarRelatorioOcupacao(any())).thenReturn(mockResponse);

        mockMvc.perform(get("/reports/ocupacao")
                .param("periodo", "2025/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.periodo").value("2025/2"))
                .andExpect(jsonPath("$.ocupacaoPorSala").isArray())
                .andExpect(jsonPath("$.ocupacaoPorSala[0].salaId").value(1))
                .andExpect(jsonPath("$.ocupacaoPorSala[0].salaNome").value("Sala A1"))
                .andExpect(jsonPath("$.ocupacaoPorCurso").isArray())
                .andExpect(jsonPath("$.ocupacaoPorCurso[0].cursoNome").value("Ciência da Computação"))
                .andExpect(jsonPath("$.ocupacaoPorDisciplina").isArray())
                .andExpect(jsonPath("$.resumo.totalSalas").value(10))
                .andExpect(jsonPath("$.resumo.taxaOcupacaoGeral").value(30.50));
    }

    @Test
    void gerarRelatorioOcupacao_ComFiltros_DeveRetornarRelatorioFiltrado() throws Exception {
        when(reportsService.gerarRelatorioOcupacao(any())).thenReturn(mockResponse);

        mockMvc.perform(get("/reports/ocupacao")
                .param("periodo", "2025/2")
                .param("cursoId", "1")
                .param("disciplinaId", "1")
                .param("salaId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.periodo").value("2025/2"));
    }

    @Test
    void gerarRelatorioOcupacao_PeriodoInvalido_DeveRetornarBadRequest() throws Exception {
        mockMvc.perform(get("/reports/ocupacao")
                .param("periodo", "2025/3") // Semestre inválido
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void gerarRelatorioOcupacao_PeriodoVazio_DeveRetornarBadRequest() throws Exception {
        mockMvc.perform(get("/reports/ocupacao")
                .param("periodo", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void gerarRelatorioOcupacao_SemPeriodo_DeveRetornarBadRequest() throws Exception {
        mockMvc.perform(get("/reports/ocupacao")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gerarRelatorioOcupacao_PeriodoFormatoIncorreto_DeveRetornarBadRequest() throws Exception {
        mockMvc.perform(get("/reports/ocupacao")
                .param("periodo", "20252") // Formato incorreto
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
