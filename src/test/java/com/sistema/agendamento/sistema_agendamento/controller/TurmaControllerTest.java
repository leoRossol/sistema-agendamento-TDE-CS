//package com.sistema.agendamento.sistema_agendamento.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sistema.agendamento.sistema_agendamento.dto.TurmaRequestDTO;
//import com.sistema.agendamento.sistema_agendamento.dto.TurmaResponseDTO;
//import com.sistema.agendamento.sistema_agendamento.exception.CodigoDuplicadoException;
//import com.sistema.agendamento.sistema_agendamento.exception.DisciplinaInvalidaException;
//import com.sistema.agendamento.sistema_agendamento.exception.ProfessorInvalidoException;
//import com.sistema.agendamento.sistema_agendamento.exception.TurmaNotFoundException;
//import com.sistema.agendamento.sistema_agendamento.service.TurmaService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(TurmaController.class)
//class TurmaControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private TurmaService turmaService;
//
//    private TurmaRequestDTO requestDTO;
//    private TurmaResponseDTO responseDTO;
//
//    @BeforeEach
//    void setUp() {
//        requestDTO = new TurmaRequestDTO();
//        requestDTO.setCodigo("ALG-2025-2");
//        requestDTO.setSemestre("2");
//        requestDTO.setAno(2025);
//        requestDTO.setDisciplinaId(1L);
//        requestDTO.setProfessorId(1L);
//
//        TurmaResponseDTO.DisciplinaSimplificadaDTO disciplinaDTO =
//            new TurmaResponseDTO.DisciplinaSimplificadaDTO(1L, "Algoritmos", "ALG101");
//
//        TurmaResponseDTO.ProfessorSimplificadoDTO professorDTO =
//            new TurmaResponseDTO.ProfessorSimplificadoDTO(1L, "Prof. João Silva", "joao@test.com");
//
//        responseDTO = new TurmaResponseDTO();
//        responseDTO.setId(1L);
//        responseDTO.setCodigo("ALG-2025-2");
//        responseDTO.setSemestre("2");
//        responseDTO.setAno(2025);
//        responseDTO.setAtivo(true);
//        responseDTO.setDisciplina(disciplinaDTO);
//        responseDTO.setProfessor(professorDTO);
//    }
//
//    @Test
//    void deveCriarTurmaComSucesso() throws Exception {
//        // Given
//        when(turmaService.criarTurma(any(TurmaRequestDTO.class))).thenReturn(responseDTO);
//
//        // When & Then
//        mockMvc.perform(post("/catalog/turmas")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(requestDTO)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.codigo").value("ALG-2025-2"))
//                .andExpect(jsonPath("$.semestre").value("2"))
//                .andExpect(jsonPath("$.ano").value(2025))
//                .andExpect(jsonPath("$.ativo").value(true))
//                .andExpect(jsonPath("$.disciplina.nome").value("Algoritmos"))
//                .andExpect(jsonPath("$.professor.nome").value("Prof. João Silva"));
//
//        verify(turmaService).criarTurma(any(TurmaRequestDTO.class));
//    }
//
//    @Test
//    void deveRetornar400QuandoPayloadInvalido() throws Exception {
//        // Given - requestDTO sem campos obrigatórios
//        TurmaRequestDTO invalidDTO = new TurmaRequestDTO();
//
//        // When & Then
//        mockMvc.perform(post("/catalog/turmas")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(invalidDTO)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value(400))
//                .andExpect(jsonPath("$.validationErrors").exists());
//
//        verify(turmaService, never()).criarTurma(any(TurmaRequestDTO.class));
//    }
//
//    @Test
//    void deveRetornar422QuandoDisciplinaInvalida() throws Exception {
//        // Given
//        when(turmaService.criarTurma(any(TurmaRequestDTO.class)))
//            .thenThrow(new DisciplinaInvalidaException(1L));
//
//        // When & Then
//        mockMvc.perform(post("/catalog/turmas")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(requestDTO)))
//                .andExpect(status().isUnprocessableEntity())
//                .andExpect(jsonPath("$.status").value(422))
//                .andExpect(jsonPath("$.message").value("Disciplina inválida com ID: 1"));
//
//        verify(turmaService).criarTurma(any(TurmaRequestDTO.class));
//    }
//
//    @Test
//    void deveRetornar422QuandoProfessorInvalido() throws Exception {
//        // Given
//        when(turmaService.criarTurma(any(TurmaRequestDTO.class)))
//            .thenThrow(new ProfessorInvalidoException(1L));
//
//        // When & Then
//        mockMvc.perform(post("/catalog/turmas")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(requestDTO)))
//                .andExpect(status().isUnprocessableEntity())
//                .andExpect(jsonPath("$.status").value(422))
//                .andExpect(jsonPath("$.message").value("Professor inválido com ID: 1"));
//
//        verify(turmaService).criarTurma(any(TurmaRequestDTO.class));
//    }
//
//    @Test
//    void deveRetornar409QuandoCodigoDuplicado() throws Exception {
//        // Given
//        when(turmaService.criarTurma(any(TurmaRequestDTO.class)))
//            .thenThrow(new CodigoDuplicadoException("ALG-2025-2", "2", 2025));
//
//        // When & Then
//        mockMvc.perform(post("/catalog/turmas")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(requestDTO)))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.status").value(409))
//                .andExpect(jsonPath("$.message").value("Código ALG-2025-2 já existe para o período 2/2025"));
//
//        verify(turmaService).criarTurma(any(TurmaRequestDTO.class));
//    }
//
//    @Test
//    void deveBuscarTurmaPorId() throws Exception {
//        // Given
//        when(turmaService.buscarTurmaPorId(1L)).thenReturn(responseDTO);
//
//        // When & Then
//        mockMvc.perform(get("/catalog/turmas/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.codigo").value("ALG-2025-2"))
//                .andExpect(jsonPath("$.disciplina.nome").value("Algoritmos"));
//
//        verify(turmaService).buscarTurmaPorId(1L);
//    }
//
//    @Test
//    void deveRetornar404QuandoTurmaNaoEncontrada() throws Exception {
//        // Given
//        when(turmaService.buscarTurmaPorId(999L))
//            .thenThrow(new TurmaNotFoundException(999L));
//
//        // When & Then
//        mockMvc.perform(get("/catalog/turmas/999"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value(404))
//                .andExpect(jsonPath("$.message").value("Turma não encontrada com ID: 999"));
//
//        verify(turmaService).buscarTurmaPorId(999L);
//    }
//
//    @Test
//    void deveListarTurmasSemFiltros() throws Exception {
//        // Given
//        List<TurmaResponseDTO> turmas = Arrays.asList(responseDTO);
//        when(turmaService.buscarTurmas(null, null)).thenReturn(turmas);
//
//        // When & Then
//        mockMvc.perform(get("/catalog/turmas"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].codigo").value("ALG-2025-2"));
//
//        verify(turmaService).buscarTurmas(null, null);
//    }
//
//    @Test
//    void deveListarTurmasComFiltroPeriodo() throws Exception {
//        // Given
//        List<TurmaResponseDTO> turmas = Arrays.asList(responseDTO);
//        when(turmaService.buscarTurmas("2025/2", null)).thenReturn(turmas);
//
//        // When & Then
//        mockMvc.perform(get("/catalog/turmas")
//                .param("periodo", "2025/2"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].semestre").value("2"))
//                .andExpect(jsonPath("$[0].ano").value(2025));
//
//        verify(turmaService).buscarTurmas("2025/2", null);
//    }
//
//    @Test
//    void deveListarTurmasComFiltroProfessor() throws Exception {
//        // Given
//        List<TurmaResponseDTO> turmas = Arrays.asList(responseDTO);
//        when(turmaService.buscarTurmas(null, 1L)).thenReturn(turmas);
//
//        // When & Then
//        mockMvc.perform(get("/catalog/turmas")
//                .param("professorId", "1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].professor.id").value(1));
//
//        verify(turmaService).buscarTurmas(null, 1L);
//    }
//
//    @Test
//    void deveListarTurmasComTodosFiltros() throws Exception {
//        // Given
//        List<TurmaResponseDTO> turmas = Arrays.asList(responseDTO);
//        when(turmaService.buscarTurmas("2025/2", 1L)).thenReturn(turmas);
//
//        // When & Then
//        mockMvc.perform(get("/catalog/turmas")
//                .param("periodo", "2025/2")
//                .param("professorId", "1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].semestre").value("2"))
//                .andExpect(jsonPath("$[0].ano").value(2025))
//                .andExpect(jsonPath("$[0].professor.id").value(1));
//
//        verify(turmaService).buscarTurmas("2025/2", 1L);
//    }
//}
//
