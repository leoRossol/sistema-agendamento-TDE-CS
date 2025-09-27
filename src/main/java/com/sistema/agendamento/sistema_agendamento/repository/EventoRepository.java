package com.sistema.agendamento.sistema_agendamento.repository;

import com.sistema.agendamento.sistema_agendamento.enums.*;
import com.sistema.agendamento.sistema_agendamento.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    
    List<Evento> findByProfessor(Usuario professor);
    
    List<Evento> findBySala(Sala sala);
    
    @Query("SELECT e FROM Evento e WHERE e.dataInicio >= :inicio AND e.dataFim <= :fim")
    List<Evento> findEventosEntreDatas(LocalDateTime inicio, LocalDateTime fim);
    
    @Query("SELECT e FROM Evento e WHERE e.sala = :sala AND " +
           "((e.dataInicio <= :inicio AND e.dataFim > :inicio) OR " +
           "(e.dataInicio < :fim AND e.dataFim >= :fim) OR " +
           "(e.dataInicio >= :inicio AND e.dataFim <= :fim))")
    List<Evento> findConflitosAgendamento(Sala sala, LocalDateTime inicio, LocalDateTime fim);
    
    @Query("SELECT e FROM Evento e WHERE DATE(e.dataInicio) = CURRENT_DATE")
    List<Evento> findEventosDeHoje();
}
