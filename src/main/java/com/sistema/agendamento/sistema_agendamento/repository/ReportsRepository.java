package com.sistema.agendamento.sistema_agendamento.repository;

import com.sistema.agendamento.sistema_agendamento.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportsRepository extends JpaRepository<Evento, Long> {

    /**
     * Busca eventos por período (ano/semestre)
     */
    @Query("SELECT e FROM Evento e " +
           "JOIN e.turma t " +
           "WHERE t.semestre = :semestre " +
           "AND t.ano = :ano " +
           "AND e.status = 'AGENDADO'")
    List<Evento> findEventosPorPeriodo(@Param("semestre") String semestre, @Param("ano") Integer ano);

    /**
     * Busca eventos por período e sala específica
     */
    @Query("SELECT e FROM Evento e " +
           "JOIN e.turma t " +
           "WHERE t.semestre = :semestre " +
           "AND t.ano = :ano " +
           "AND e.status = 'AGENDADO' " +
           "AND (:salaId IS NULL OR e.sala.id = :salaId)")
    List<Evento> findEventosPorPeriodoESala(@Param("semestre") String semestre, 
                                           @Param("ano") Integer ano, 
                                           @Param("salaId") Long salaId);

    /**
     * Busca eventos por período e curso específico
     */
    @Query("SELECT e FROM Evento e " +
           "JOIN e.turma t " +
           "JOIN t.disciplina d " +
           "JOIN d.curso c " +
           "WHERE t.semestre = :semestre " +
           "AND t.ano = :ano " +
           "AND e.status = 'AGENDADO' " +
           "AND (:cursoId IS NULL OR c.id = :cursoId)")
    List<Evento> findEventosPorPeriodoECurso(@Param("semestre") String semestre, 
                                            @Param("ano") Integer ano, 
                                            @Param("cursoId") Long cursoId);

    /**
     * Busca eventos por período e disciplina específica
     */
    @Query("SELECT e FROM Evento e " +
           "JOIN e.turma t " +
           "JOIN t.disciplina d " +
           "WHERE t.semestre = :semestre " +
           "AND t.ano = :ano " +
           "AND e.status = 'AGENDADO' " +
           "AND (:disciplinaId IS NULL OR d.id = :disciplinaId)")
    List<Evento> findEventosPorPeriodoEDisciplina(@Param("semestre") String semestre, 
                                                 @Param("ano") Integer ano, 
                                                 @Param("disciplinaId") Long disciplinaId);

    /**
     * Busca eventos por sala e período para detalhamento
     */
    @Query("SELECT e FROM Evento e " +
           "JOIN e.sala s " +
           "JOIN e.turma t " +
           "WHERE s.id = :salaId " +
           "AND t.semestre = :semestre " +
           "AND t.ano = :ano " +
           "AND e.status = 'AGENDADO' " +
           "ORDER BY e.dataInicio")
    List<Evento> findEventosPorSalaEPeriodo(@Param("salaId") Long salaId,
                                           @Param("semestre") String semestre,
                                           @Param("ano") Integer ano);
}