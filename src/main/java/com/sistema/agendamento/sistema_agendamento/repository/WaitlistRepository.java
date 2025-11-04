package com.sistema.agendamento.sistema_agendamento.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sistema.agendamento.sistema_agendamento.entity.Sala;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.entity.WaitlistEntry;
import com.sistema.agendamento.sistema_agendamento.entity.WaitlistEntry.Status;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {

    long countBySalaAndStatusIn(Sala sala, List<Status> statuses);

    @Query("""
           select w
           from WaitlistEntry w
           where w.sala = :sala
             and w.status in :statuses
           order by w.createdAt asc
           """)
    List<WaitlistEntry> findQueueBySala(@Param("sala") Sala sala,
                                        @Param("statuses") List<Status> statuses);

    @Query("""
           select w
           from WaitlistEntry w
           where w.sala = :sala
             and w.status = 'WAITING'
             and w.janelaInicio < :fim
             and w.janelaFim    > :inicio
           order by w.createdAt asc
           """)
    List<WaitlistEntry> findWaitingOverlapping(@Param("sala") Sala sala,
                                               @Param("inicio") LocalDateTime inicio,
                                               @Param("fim") LocalDateTime fim);

    long countBySalaAndStatusInAndCreatedAtBefore(Sala sala, List<Status> statuses, LocalDateTime createdAt);

    List<WaitlistEntry> findByProfessorAndStatusIn(Usuario professor, List<Status> statuses);
}
