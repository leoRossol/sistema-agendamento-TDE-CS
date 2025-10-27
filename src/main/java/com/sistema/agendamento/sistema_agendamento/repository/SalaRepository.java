package com.sistema.agendamento.sistema_agendamento.repository;

import com.sistema.agendamento.sistema_agendamento.entity.Sala;
import com.sistema.agendamento.sistema_agendamento.enums.TipoSala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Long> {
    
    List<Sala> findByAtivoTrue();
    
    List<Sala> findByTipoSala(TipoSala tipoSala);
    
    @Query("SELECT s FROM Sala s WHERE s.capacidade >= :capacidade AND s.ativo = true")
    List<Sala> findByCapacidadeMinima(Integer capacidade);
    
    @Query("SELECT s FROM Sala s WHERE s NOT IN " +
           "(SELECT e.sala FROM Evento e WHERE " +
           "((e.dataInicio <= :inicio AND e.dataFim > :inicio) OR " +
           "(e.dataInicio < :fim AND e.dataFim >= :fim) OR " +
           "(e.dataInicio >= :inicio AND e.dataFim <= :fim))) " +
           "AND s.ativo = true")
    List<Sala> findSalasDisponiveis(LocalDateTime inicio, LocalDateTime fim);
}
