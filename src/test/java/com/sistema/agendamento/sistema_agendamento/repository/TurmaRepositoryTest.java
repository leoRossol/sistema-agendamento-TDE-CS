package com.sistema.agendamento.sistema_agendamento.repository;

import com.sistema.agendamento.sistema_agendamento.entity.Curso;
import com.sistema.agendamento.sistema_agendamento.entity.Disciplina;
import com.sistema.agendamento.sistema_agendamento.entity.Turma;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TurmaRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private TurmaRepository turmaRepository;
    
    private Disciplina disciplina;
    private Usuario professor;
    private Turma turma;
    
    @BeforeEach
    void setUp() {
        // Criar Curso
        Curso curso = new Curso();
        curso.setNome("Ciência da Computação");
        curso.setCodigo("CC");
        entityManager.persist(curso);
        
        // Criar Disciplina
        disciplina = new Disciplina();
        disciplina.setNome("Algoritmos");
        disciplina.setCodigo("ALG101");
        disciplina.setCargaHoraria(60);
        disciplina.setCurso(curso);
        entityManager.persist(disciplina);
        
        // Criar Professor
        professor = new Usuario();
        professor.setNome("Prof. João Silva");
        professor.setEmail("joao@test.com");
        professor.setSenha("senha123");
        professor.setTipoUsuario(TipoUsuario.PROFESSOR);
        professor.setAtivo(true);
        entityManager.persist(professor);
        
        // Criar Turma
        turma = new Turma();
        turma.setCodigo("ALG-2025-2");
        turma.setSemestre("2");
        turma.setAno(2025);
        turma.setDisciplina(disciplina);
        turma.setProfessor(professor);
        turma.setAtivo(true);
        entityManager.persist(turma);
        
        entityManager.flush();
    }
    
    @Test
    void deveVerificarSeCodigoExisteParaPeriodo() {
        // Given - turma já criada no setUp
        
        // When
        boolean existe = turmaRepository.existsByCodigoAndSemestreAndAno("ALG-2025-2", "2", 2025);
        boolean naoExiste = turmaRepository.existsByCodigoAndSemestreAndAno("ALG-2025-1", "1", 2025);
        
        // Then
        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }
    
    @Test
    void devePermitirMesmoCodigoEmPeriodoDiferente() {
        // Given
        Turma outraTurma = new Turma();
        outraTurma.setCodigo("ALG-2025-2"); // Mesmo código
        outraTurma.setSemestre("1"); // Semestre diferente
        outraTurma.setAno(2025);
        outraTurma.setDisciplina(disciplina);
        outraTurma.setProfessor(professor);
        outraTurma.setAtivo(true);
        entityManager.persist(outraTurma);
        entityManager.flush();
        
        // When
        boolean existeSemestre2 = turmaRepository.existsByCodigoAndSemestreAndAno("ALG-2025-2", "2", 2025);
        boolean existeSemestre1 = turmaRepository.existsByCodigoAndSemestreAndAno("ALG-2025-2", "1", 2025);
        
        // Then
        assertThat(existeSemestre2).isTrue();
        assertThat(existeSemestre1).isTrue();
    }
    
    @Test
    void deveBuscarTurmasPorSemestreAnoEProfessor() {
        // Given
        Usuario outroProfessor = new Usuario();
        outroProfessor.setNome("Prof. Maria Santos");
        outroProfessor.setEmail("maria@test.com");
        outroProfessor.setSenha("senha123");
        outroProfessor.setTipoUsuario(TipoUsuario.PROFESSOR);
        outroProfessor.setAtivo(true);
        entityManager.persist(outroProfessor);
        
        Turma outraTurma = new Turma();
        outraTurma.setCodigo("ALG-2025-2-B");
        outraTurma.setSemestre("2");
        outraTurma.setAno(2025);
        outraTurma.setDisciplina(disciplina);
        outraTurma.setProfessor(outroProfessor);
        outraTurma.setAtivo(true);
        entityManager.persist(outraTurma);
        entityManager.flush();
        
        // When
        List<Turma> turmasProfessor1 = turmaRepository.findBySemestreAndAnoAndProfessorId("2", 2025, professor.getId());
        List<Turma> turmasProfessor2 = turmaRepository.findBySemestreAndAnoAndProfessorId("2", 2025, outroProfessor.getId());
        
        // Then
        assertThat(turmasProfessor1).hasSize(1);
        assertThat(turmasProfessor1.get(0).getCodigo()).isEqualTo("ALG-2025-2");
        
        assertThat(turmasProfessor2).hasSize(1);
        assertThat(turmasProfessor2.get(0).getCodigo()).isEqualTo("ALG-2025-2-B");
    }
    
    @Test
    void deveBuscarTurmasPorSemestreEAno() {
        // Given - turma já criada no setUp
        
        Turma outraTurma = new Turma();
        outraTurma.setCodigo("ALG-2024-2");
        outraTurma.setSemestre("2");
        outraTurma.setAno(2024);
        outraTurma.setDisciplina(disciplina);
        outraTurma.setProfessor(professor);
        outraTurma.setAtivo(true);
        entityManager.persist(outraTurma);
        entityManager.flush();
        
        // When
        List<Turma> turmas2025 = turmaRepository.findBySemestreAndAno("2", 2025);
        List<Turma> turmas2024 = turmaRepository.findBySemestreAndAno("2", 2024);
        
        // Then
        assertThat(turmas2025).hasSize(1);
        assertThat(turmas2025.get(0).getCodigo()).isEqualTo("ALG-2025-2");
        
        assertThat(turmas2024).hasSize(1);
        assertThat(turmas2024.get(0).getCodigo()).isEqualTo("ALG-2024-2");
    }
}

