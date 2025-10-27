package com.sistema.agendamento.sistema_agendamento.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sistema.agendamento.sistema_agendamento.entity.Evento;
import com.sistema.agendamento.sistema_agendamento.entity.Sala;
import com.sistema.agendamento.sistema_agendamento.entity.Turma;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByProfessor(Usuario professor);

    List<Evento> findBySala(Sala sala);

    /**
     * Eventos que INTERSECTAM o período [inicio, fim)
     * (inicio incluso, fim exclusivo).
     */
    @Query("""
           select e
           from Evento e
           where e.dataInicio < :fim
             and e.dataFim    > :inicio
           """)
    List<Evento> findEventosDoPeriodo(@Param("inicio") LocalDateTime inicio,
                                      @Param("fim")    LocalDateTime fim);

    /**
     * Versão “entre datas” antiga — se quiser manter o nome, deixe
     * este método delegar para o canônico de período.
     */
    default List<Evento> findEventosEntreDatas(LocalDateTime inicio, LocalDateTime fim) {
        return findEventosDoPeriodo(inicio, fim);
    }

    /**
     * Conflito de sala: qualquer evento na mesma sala que intersecte o período.
     */
    @Query("""
           select e
           from Evento e
           where e.sala = :sala
             and e.dataInicio < :fim
             and e.dataFim    > :inicio
           """)
    List<Evento> findConflitosAgendamento(@Param("sala") Sala sala,
                                          @Param("inicio") LocalDateTime inicio,
                                          @Param("fim")    LocalDateTime fim);

    /**
     * Conflito de professor: interseção no período.
     */
    @Query("""
           select e
           from Evento e
           where e.professor = :professor
             and e.dataInicio < :fim
             and e.dataFim    > :inicio
           """)
    List<Evento> findConflitosProfessor(@Param("professor") Usuario professor,
                                        @Param("inicio") LocalDateTime inicio,
                                        @Param("fim")    LocalDateTime fim);

    /**
     * Conflito de turma: interseção no período.
     */
    @Query("""
           select e
           from Evento e
           where e.turma = :turma
             and e.dataInicio < :fim
             and e.dataFim    > :inicio
           """)
    List<Evento> findConflitosTurma(@Param("turma") Turma turma,
                                    @Param("inicio") LocalDateTime inicio,
                                    @Param("fim")    LocalDateTime fim);

    /**
     * “Hoje” sem usar funções de data no JPQL (evita problemas de dialeto).
     */
    default List<Evento> findEventosDeHoje() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = inicio.plusDays(1);
        return findEventosDoPeriodo(inicio, fim);
    }
}