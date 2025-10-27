package com.sistema.agendamento.sistema_agendamento.repository;

import com.sistema.agendamento.sistema_agendamento.entity.*;
import com.sistema.agendamento.sistema_agendamento.enums.StatusEventos;
import com.sistema.agendamento.sistema_agendamento.enums.TipoEvento;
import com.sistema.agendamento.sistema_agendamento.enums.TipoSala;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReportsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReportsRepository reportsRepository;

    private Curso curso;
    private Disciplina disciplina;
    private Usuario professor;
    private Turma turma;
    private Sala sala;
    private Evento evento;

    @BeforeEach
    void setUp() {
        // Criar Curso
        curso = new Curso();
        curso.setNome("Ciência da Computação");
        curso.setCodigo("CC");
        entityManager.persist(curso);

        // Criar Disciplina
        disciplina = new Disciplina();
        disciplina.setNome("Algoritmos");
        disciplina.setCodigo("ALG001");
        disciplina.setCargaHoraria(60);
        disciplina.setCurso(curso);
        entityManager.persist(disciplina);

        // Criar Professor
        professor = new Usuario();
        professor.setNome("Prof. João Silva");
        professor.setEmail("joao@test.com");
        professor.setSenha("senha123");
        professor.setTipoUsuario(TipoUsuario.PROFESSOR);
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

        // Criar Sala
        sala = new Sala();
        sala.setNome("Sala A1");
        sala.setNumero("A1");
        sala.setTipoSala(TipoSala.SALA_AULA);
        sala.setCapacidade(50);
        sala.setAtivo(true);
        entityManager.persist(sala);

        // Criar Evento
        evento = new Evento();
        evento.setTitulo("Aula de Algoritmos");
        evento.setDataInicio(LocalDateTime.of(2025, 8, 15, 8, 0));
        evento.setDataFim(LocalDateTime.of(2025, 8, 15, 10, 0));
        evento.setStatus(StatusEventos.AGENDADO);
        evento.setTipoEvento(TipoEvento.AULA);
        evento.setTurma(turma);
        evento.setSala(sala);
        evento.setProfessor(professor);
        entityManager.persist(evento);

        entityManager.flush();
    }

    @Test
    void findEventosPorPeriodo_ComDadosValidos_DeveRetornarEventos() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodo("2", 2025);

        // Then
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).getTitulo()).isEqualTo("Aula de Algoritmos");
        assertThat(eventos.get(0).getTurma().getSemestre()).isEqualTo("2");
        assertThat(eventos.get(0).getTurma().getAno()).isEqualTo(2025);
    }

    @Test
    void findEventosPorPeriodo_ComPeriodoDiferente_DeveRetornarListaVazia() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodo("1", 2025);

        // Then
        assertThat(eventos).isEmpty();
    }

    @Test
    void findEventosPorPeriodoESala_ComSalaValida_DeveRetornarEventos() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodoESala("2", 2025, sala.getId());

        // Then
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).getSala().getId()).isEqualTo(sala.getId());
    }

    @Test
    void findEventosPorPeriodoESala_ComSalaInvalida_DeveRetornarListaVazia() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodoESala("2", 2025, 999L);

        // Then
        assertThat(eventos).isEmpty();
    }

    @Test
    void findEventosPorPeriodoESala_ComSalaNull_DeveRetornarTodosEventos() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodoESala("2", 2025, null);

        // Then
        assertThat(eventos).hasSize(1);
    }

    @Test
    void findEventosPorPeriodoECurso_ComCursoValido_DeveRetornarEventos() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodoECurso("2", 2025, curso.getId());

        // Then
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).getTurma().getDisciplina().getCurso().getId()).isEqualTo(curso.getId());
    }

    @Test
    void findEventosPorPeriodoECurso_ComCursoInvalido_DeveRetornarListaVazia() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodoECurso("2", 2025, 999L);

        // Then
        assertThat(eventos).isEmpty();
    }

    @Test
    void findEventosPorPeriodoECurso_ComCursoNull_DeveRetornarTodosEventos() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodoECurso("2", 2025, null);

        // Then
        assertThat(eventos).hasSize(1);
    }

    @Test
    void findEventosPorPeriodoEDisciplina_ComDisciplinaValida_DeveRetornarEventos() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodoEDisciplina("2", 2025, disciplina.getId());

        // Then
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).getTurma().getDisciplina().getId()).isEqualTo(disciplina.getId());
    }

    @Test
    void findEventosPorPeriodoEDisciplina_ComDisciplinaInvalida_DeveRetornarListaVazia() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodoEDisciplina("2", 2025, 999L);

        // Then
        assertThat(eventos).isEmpty();
    }

    @Test
    void findEventosPorPeriodoEDisciplina_ComDisciplinaNull_DeveRetornarTodosEventos() {
        // When
        List<Evento> eventos = reportsRepository.findEventosPorPeriodoEDisciplina("2", 2025, null);

        // Then
        assertThat(eventos).hasSize(1);
    }

    @Test
    void findEventosPorSalaEPeriodo_ComSalaValida_DeveRetornarEventosOrdenados() {
        // Criar outro evento para testar ordenação
        Evento evento2 = new Evento();
        evento2.setTitulo("Aula de Estruturas");
        evento2.setDataInicio(LocalDateTime.of(2025, 8, 16, 14, 0));
        evento2.setDataFim(LocalDateTime.of(2025, 8, 16, 16, 0));
        evento2.setStatus(StatusEventos.AGENDADO);
        evento2.setTipoEvento(TipoEvento.AULA);
        evento2.setTurma(turma);
        evento2.setSala(sala);
        evento2.setProfessor(professor);
        entityManager.persist(evento2);
        entityManager.flush();

        // When
        List<Evento> eventos = reportsRepository.findEventosPorSalaEPeriodo(sala.getId(), "2", 2025);

        // Then
        assertThat(eventos).hasSize(2);
        assertThat(eventos.get(0).getDataInicio()).isBefore(eventos.get(1).getDataInicio());
    }
}
