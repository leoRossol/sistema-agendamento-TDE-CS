package com.sistema.agendamento.sistema_agendamento.repository;

import com.sistema.agendamento.sistema_agendamento.entity.ReservaSala;
import com.sistema.agendamento.sistema_agendamento.entity.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



import java.time.LocalDateTime;
import java.util.List;

public interface ReservaSalaRepository extends JpaRepository<ReservaSala, Long> {
    
    //===US01===
    @Query("""
            SELECT (COUNT(r) > 0)
            FROM ReservaSala r
            WHERE r.sala.id = :salaId
                AND r.dataInicio < :fim
                AND r.dataFim > :inicio
    """)
    boolean temConflito(@Param("salaId") Long salaId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);   
    
    @Query("""
            SELECT r
            FROM ReservaSala r
            WHERE r.sala.id = :salaId
                AND r.dataInicio BETWEEN :inicio AND :fim
            ORDER BY r.dataInicio 
            """)
    List<ReservaSala> findaBySalaIdAndInicioBetween(Long salaId, LocalDateTime inicio, LocalDateTime fim);


    // ===US07===
    @Query ("""
            SELECT DISTINCT r.sala
            FROM ReservaSala r
            WHERE UPPER(r.status) = 'APROVADA'
            ORDER BY r.sala.nome
            """)
    List<Sala> todasSalasReservadas();

    @Query ("""
            SELECT DISTINCT r.sala
            FROM ReservaSala r
            WHERE r.dataInicio < :to
                AND r.dataFim > :from
                AND UPPER(r.status) = 'APROVADA'
            ORDER BY r.sala.nome
            """)
    List<Sala> salasReservadasNoPeriodo(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
