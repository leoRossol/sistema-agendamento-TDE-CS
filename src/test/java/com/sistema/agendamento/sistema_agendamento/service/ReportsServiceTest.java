package com.sistema.agendamento.sistema_agendamento.service;

import com.sistema.agendamento.sistema_agendamento.dto.RelatorioOcupacaoRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.RelatorioOcupacaoResponseDTO;
import com.sistema.agendamento.sistema_agendamento.entity.*;
import com.sistema.agendamento.sistema_agendamento.enums.StatusEventos;
import com.sistema.agendamento.sistema_agendamento.enums.TipoEvento;
import com.sistema.agendamento.sistema_agendamento.enums.TipoSala;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
import com.sistema.agendamento.sistema_agendamento.repository.ReportsRepository;
import com.sistema.agendamento.sistema_agendamento.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportsServiceTest {

    @Mock
    private ReportsRepository reportsRepository;

    @Mock
    private SalaRepository salaRepository;

    @InjectMocks
    private ReportsService reportsService;

    private RelatorioOcupacaoRequestDTO requestDTO;
    private Evento mockEvento;
    private Sala mockSala;
    private Turma mockTurma;
    private Disciplina mockDisciplina;
    private Curso mockCurso;
    private Usuario mockProfessor;

    @BeforeEach
    void setUp() {
        requestDTO = new RelatorioOcupacaoRequestDTO();
        requestDTO.setPeriodo("2025/2");

        // Criar entidades mock
        mockProfessor = new Usuario();
        mockProfessor.setId(1L);
        mockProfessor.setNome("Professor Teste");
        mockProfessor.setTipoUsuario(TipoUsuario.PROFESSOR);

        mockCurso = new Curso();
        mockCurso.setId(1L);
        mockCurso.setNome("Ciência da Computação");
        mockCurso.setCodigo("CC");

        mockDisciplina = new Disciplina();
        mockDisciplina.setId(1L);
        mockDisciplina.setNome("Algoritmos");
        mockDisciplina.setCodigo("ALG001");
        mockDisciplina.setCurso(mockCurso);

        mockTurma = new Turma();
        mockTurma.setId(1L);
        mockTurma.setCodigo("ALG-2025-2");
        mockTurma.setSemestre("2");
        mockTurma.setAno(2025);
        mockTurma.setDisciplina(mockDisciplina);
        mockTurma.setProfessor(mockProfessor);

        mockSala = new Sala();
        mockSala.setId(1L);
        mockSala.setNome("Sala A1");
        mockSala.setNumero("A1");
        mockSala.setCapacidade(50);

        mockEvento = new Evento();
        mockEvento.setId(1L);
        mockEvento.setTitulo("Aula de Algoritmos");
        mockEvento.setDataInicio(LocalDateTime.of(2025, 8, 15, 8, 0));
        mockEvento.setDataFim(LocalDateTime.of(2025, 8, 15, 10, 0));
        mockEvento.setStatus(StatusEventos.AGENDADO);
        mockEvento.setTipoEvento(TipoEvento.AULA);
        mockEvento.setTurma(mockTurma);
        mockEvento.setSala(mockSala);
        mockEvento.setProfessor(mockProfessor);
    }

    @Test
    void gerarRelatorioOcupacao_ComDadosValidos_DeveRetornarRelatorioCompleto() {
        // Given
        when(reportsRepository.findEventosPorPeriodo(anyString(), anyInt()))
            .thenReturn(Arrays.asList(mockEvento));
        when(salaRepository.findByAtivoTrue())
            .thenReturn(Arrays.asList(mockSala));

        // When
        RelatorioOcupacaoResponseDTO response = reportsService.gerarRelatorioOcupacao(requestDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPeriodo()).isEqualTo("2025/2");
        assertThat(response.getOcupacaoPorSala()).hasSize(1);
        assertThat(response.getOcupacaoPorCurso()).hasSize(1);
        assertThat(response.getOcupacaoPorDisciplina()).hasSize(1);
        assertThat(response.getResumo()).isNotNull();

        // Verificar ocupação por sala
        RelatorioOcupacaoResponseDTO.OcupacaoPorSalaDTO ocupacaoSala = response.getOcupacaoPorSala().get(0);
        assertThat(ocupacaoSala.getSalaId()).isEqualTo(1L);
        assertThat(ocupacaoSala.getSalaNome()).isEqualTo("Sala A1");
        assertThat(ocupacaoSala.getTotalHorasUtilizadas()).isEqualTo(2);
        assertThat(ocupacaoSala.getTaxaOcupacao()).isNotNull();

        // Verificar ocupação por curso
        RelatorioOcupacaoResponseDTO.OcupacaoPorCursoDTO ocupacaoCurso = response.getOcupacaoPorCurso().get(0);
        assertThat(ocupacaoCurso.getCursoId()).isEqualTo(1L);
        assertThat(ocupacaoCurso.getCursoNome()).isEqualTo("Ciência da Computação");

        // Verificar resumo
        RelatorioOcupacaoResponseDTO.ResumoOcupacaoDTO resumo = response.getResumo();
        assertThat(resumo.getTotalSalas()).isEqualTo(1);
        assertThat(resumo.getSalasUtilizadas()).isEqualTo(1);
    }

    @Test
    void gerarRelatorioOcupacao_ComFiltroPorSala_DeveUsarMetodoCorreto() {
        // Given
        requestDTO.setSalaId(1L);
        when(reportsRepository.findEventosPorPeriodoESala(anyString(), anyInt(), anyLong()))
            .thenReturn(Arrays.asList(mockEvento));
        when(salaRepository.findByAtivoTrue())
            .thenReturn(Arrays.asList(mockSala));

        // When
        RelatorioOcupacaoResponseDTO response = reportsService.gerarRelatorioOcupacao(requestDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPeriodo()).isEqualTo("2025/2");
    }

    @Test
    void gerarRelatorioOcupacao_ComFiltroPorCurso_DeveUsarMetodoCorreto() {
        // Given
        requestDTO.setCursoId(1L);
        when(reportsRepository.findEventosPorPeriodoECurso(anyString(), anyInt(), anyLong()))
            .thenReturn(Arrays.asList(mockEvento));
        when(salaRepository.findByAtivoTrue())
            .thenReturn(Arrays.asList(mockSala));

        // When
        RelatorioOcupacaoResponseDTO response = reportsService.gerarRelatorioOcupacao(requestDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPeriodo()).isEqualTo("2025/2");
    }

    @Test
    void gerarRelatorioOcupacao_ComFiltroPorDisciplina_DeveUsarMetodoCorreto() {
        // Given
        requestDTO.setDisciplinaId(1L);
        when(reportsRepository.findEventosPorPeriodoEDisciplina(anyString(), anyInt(), anyLong()))
            .thenReturn(Arrays.asList(mockEvento));
        when(salaRepository.findByAtivoTrue())
            .thenReturn(Arrays.asList(mockSala));

        // When
        RelatorioOcupacaoResponseDTO response = reportsService.gerarRelatorioOcupacao(requestDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPeriodo()).isEqualTo("2025/2");
    }

    @Test
    void gerarRelatorioOcupacao_SemEventos_DeveRetornarRelatorioVazio() {
        // Given
        when(reportsRepository.findEventosPorPeriodo(anyString(), anyInt()))
            .thenReturn(Collections.emptyList());
        when(salaRepository.findByAtivoTrue())
            .thenReturn(Arrays.asList(mockSala));

        // When
        RelatorioOcupacaoResponseDTO response = reportsService.gerarRelatorioOcupacao(requestDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPeriodo()).isEqualTo("2025/2");
        assertThat(response.getOcupacaoPorSala()).isEmpty();
        assertThat(response.getOcupacaoPorCurso()).isEmpty();
        assertThat(response.getOcupacaoPorDisciplina()).isEmpty();
        assertThat(response.getResumo()).isNotNull();
    }

    @Test
    void gerarRelatorioOcupacao_ComEventosMultiplasSalas_DeveAgruparCorretamente() {
        // Given
        Sala sala2 = new Sala();
        sala2.setId(2L);
        sala2.setNome("Sala B1");
        sala2.setNumero("B1");
        sala2.setTipoSala(TipoSala.SALA_AULA);
        sala2.setCapacidade(30);

        Evento evento2 = new Evento();
        evento2.setId(2L);
        evento2.setTitulo("Aula de Estruturas");
        evento2.setDataInicio(LocalDateTime.of(2025, 8, 16, 14, 0));
        evento2.setDataFim(LocalDateTime.of(2025, 8, 16, 16, 0));
        evento2.setStatus(StatusEventos.AGENDADO);
        evento2.setTipoEvento(TipoEvento.AULA);
        evento2.setTurma(mockTurma);
        evento2.setSala(sala2);
        evento2.setProfessor(mockProfessor);

        when(reportsRepository.findEventosPorPeriodo(anyString(), anyInt()))
            .thenReturn(Arrays.asList(mockEvento, evento2));
        when(salaRepository.findByAtivoTrue())
            .thenReturn(Arrays.asList(mockSala, sala2));

        // When
        RelatorioOcupacaoResponseDTO response = reportsService.gerarRelatorioOcupacao(requestDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOcupacaoPorSala()).hasSize(2);
        assertThat(response.getResumo().getTotalSalas()).isEqualTo(2);
        assertThat(response.getResumo().getSalasUtilizadas()).isEqualTo(2);
    }

    @Test
    void calcularTaxaOcupacao_ComHorasDisponiveisZero_DeveRetornarZero() {
        // Given
        when(reportsRepository.findEventosPorPeriodo(anyString(), anyInt()))
            .thenReturn(Arrays.asList(mockEvento));
        when(salaRepository.findByAtivoTrue())
            .thenReturn(Collections.emptyList());

        // When
        RelatorioOcupacaoResponseDTO response = reportsService.gerarRelatorioOcupacao(requestDTO);

        // Then
        assertThat(response.getResumo().getTaxaOcupacaoGeral()).isEqualTo(BigDecimal.ZERO);
    }
}
