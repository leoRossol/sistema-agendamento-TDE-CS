package com.sistema.agendamento.sistema_agendamento.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas_sala")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class ReservaSala {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Usuario professor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;
    
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;
    
    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;
    
    @Column(length = 200)
    private String motivo;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'PENDENTE'")
    private StatusReserva status = StatusReserva.PENDENTE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovada_por")
    private Usuario aprovadaPor;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum StatusReserva {
        PENDENTE, APROVADA, REJEITADA, CANCELADA
    }
}