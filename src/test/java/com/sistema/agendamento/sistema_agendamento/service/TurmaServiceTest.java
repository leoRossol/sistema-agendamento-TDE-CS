package com.sistema.agendamento.sistema_agendamento.service;

import com.sistema.agendamento.sistema_agendamento.dto.TurmaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.TurmaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Disciplina;
import com.sistema.agendamento.sistema_agendamento.entity.Turma;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
import com.sistema.agendamento.sistema_agendamento.exception.CodigoDuplicadoException;
import com.sistema.agendamento.sistema_agendamento.exception.DisciplinaInvalidaException;
import com.sistema.agendamento.sistema_agendamento.exception.ProfessorInvalidoException;
import com.sistema.agendamento.sistema_agendamento.exception.TurmaNotFoundException;
import com.sistema.agendamento.sistema_agendamento.repository.DisciplinaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.TurmaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {
    
    @Mock
    private TurmaRepository turmaRepository;
    
    @Mock
    private DisciplinaRepository disciplinaRepository;
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @InjectMocks
    private TurmaService turmaService;
    
    private TurmaRequestDTO requestDTO;
    private Disciplina disciplina;
    private Usuario professor;
    private Turma turma;
    
    @BeforeEach
    void setUp() {
        requestDTO = new TurmaRequestDTO();
        requestDTO.setCodigo("ALG-2025-2");
        requestDTO.setSemestre("2");
        requestDTO.setAno(2025);
        requestDTO.setDisciplinaId(1L);
        requestDTO.setProfessorId(1L);
        
        disciplina = new Disciplina();
        disciplina.setId(1L);
        disciplina.setNome("Algoritmos");
        disciplina.setCodigo("ALG101");
        
        professor = new Usuario("Prof. João Silva", "joao@test.com", "senha123" , TipoUsuario.PROFESSOR);
        professor.setId(1L);
        turma = new Turma();
        turma.setId(1L);
        turma.setCodigo("ALG-2025-2");
        turma.setSemestre("2");
        turma.setAno(2025);
        turma.setDisciplina(disciplina);
        turma.setProfessor(professor);
        turma.setAtivo(true);
    }
    
    @Test
    void deveCriarTurmaComSucesso() {
        // Given
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(turmaRepository.existsByCodigoAndSemestreAndAno(anyString(), anyString(), anyInt())).thenReturn(false);
        when(turmaRepository.save(any(Turma.class))).thenReturn(turma);
        
        // When
        TurmaResponseDTO response = turmaService.criarTurma(requestDTO);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCodigo()).isEqualTo("ALG-2025-2");
        assertThat(response.getSemestre()).isEqualTo("2");
        assertThat(response.getAno()).isEqualTo(2025);
        assertThat(response.getAtivo()).isTrue();
        assertThat(response.getDisciplina().getNome()).isEqualTo("Algoritmos");
        assertThat(response.getProfessor().getNome()).isEqualTo("Prof. João Silva");
        
        verify(disciplinaRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(turmaRepository).existsByCodigoAndSemestreAndAno("ALG-2025-2", "2", 2025);
        verify(turmaRepository).save(any(Turma.class));
    }
    
    @Test
    void deveLancarExcecaoQuandoDisciplinaNaoExiste() {
        // Given
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> turmaService.criarTurma(requestDTO))
            .isInstanceOf(DisciplinaInvalidaException.class)
            .hasMessageContaining("Disciplina inválida com ID: 1");
        
        verify(disciplinaRepository).findById(1L);
        verify(usuarioRepository, never()).findById(anyLong());
        verify(turmaRepository, never()).save(any(Turma.class));
    }
    
    @Test
    void deveLancarExcecaoQuandoProfessorNaoExiste() {
        // Given
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> turmaService.criarTurma(requestDTO))
            .isInstanceOf(ProfessorInvalidoException.class)
            .hasMessageContaining("Professor inválido com ID: 1");
        
        verify(disciplinaRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(turmaRepository, never()).save(any(Turma.class));
    }
    
    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEProfessor() {
        // Given
        professor.setTipoUsuario(TipoUsuario.ALUNO);
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(professor));
        
        // When & Then
        assertThatThrownBy(() -> turmaService.criarTurma(requestDTO))
            .isInstanceOf(ProfessorInvalidoException.class)
            .hasMessageContaining("não é um professor");
        
        verify(disciplinaRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(turmaRepository, never()).save(any(Turma.class));
    }
    
    @Test
    void deveLancarExcecaoQuandoCodigoDuplicado() {
        // Given
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(turmaRepository.existsByCodigoAndSemestreAndAno("ALG-2025-2", "2", 2025)).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> turmaService.criarTurma(requestDTO))
            .isInstanceOf(CodigoDuplicadoException.class)
            .hasMessageContaining("já existe para o período");
        
        verify(disciplinaRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(turmaRepository).existsByCodigoAndSemestreAndAno("ALG-2025-2", "2", 2025);
        verify(turmaRepository, never()).save(any(Turma.class));
    }
    
    @Test
    void deveBuscarTurmaPorId() {
        // Given
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        
        // When
        TurmaResponseDTO response = turmaService.buscarTurmaPorId(1L);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCodigo()).isEqualTo("ALG-2025-2");
        
        verify(turmaRepository).findById(1L);
    }
    
    @Test
    void deveLancarExcecaoQuandoTurmaNaoEncontrada() {
        // Given
        when(turmaRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> turmaService.buscarTurmaPorId(1L))
            .isInstanceOf(TurmaNotFoundException.class)
            .hasMessageContaining("Turma não encontrada com ID: 1");
        
        verify(turmaRepository).findById(1L);
    }
    
    @Test
    void deveBuscarTurmasSemFiltros() {
        // Given
        List<Turma> turmas = Arrays.asList(turma);
        when(turmaRepository.findAll()).thenReturn(turmas);
        
        // When
        List<TurmaResponseDTO> response = turmaService.buscarTurmas(null, null);
        
        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCodigo()).isEqualTo("ALG-2025-2");
        
        verify(turmaRepository).findAll();
    }
    
    @Test
    void deveBuscarTurmasComFiltroPeriodo() {
        // Given
        List<Turma> turmas = Arrays.asList(turma);
        when(turmaRepository.findBySemestreAndAno("2", 2025)).thenReturn(turmas);
        
        // When
        List<TurmaResponseDTO> response = turmaService.buscarTurmas("2025/2", null);
        
        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCodigo()).isEqualTo("ALG-2025-2");
        
        verify(turmaRepository).findBySemestreAndAno("2", 2025);
    }
    
    @Test
    void deveBuscarTurmasComFiltroProfessor() {
        // Given
        List<Turma> turmas = Arrays.asList(turma);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(turmaRepository.findByProfessor(professor)).thenReturn(turmas);
        
        // When
        List<TurmaResponseDTO> response = turmaService.buscarTurmas(null, 1L);
        
        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCodigo()).isEqualTo("ALG-2025-2");
        
        verify(usuarioRepository).findById(1L);
        verify(turmaRepository).findByProfessor(professor);
    }
    
    @Test
    void deveBuscarTurmasComPeriodoEProfessor() {
        // Given
        List<Turma> turmas = Arrays.asList(turma);
        when(turmaRepository.findBySemestreAndAnoAndProfessorId("2", 2025, 1L)).thenReturn(turmas);
        
        // When
        List<TurmaResponseDTO> response = turmaService.buscarTurmas("2025/2", 1L);
        
        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCodigo()).isEqualTo("ALG-2025-2");
        
        verify(turmaRepository).findBySemestreAndAnoAndProfessorId("2", 2025, 1L);
    }
}

