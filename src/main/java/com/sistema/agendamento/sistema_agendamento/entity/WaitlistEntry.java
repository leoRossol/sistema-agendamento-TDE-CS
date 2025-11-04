package com.sistema.agendamento.sistema_agendamento.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "waitlist_entries")
public class WaitlistEntry {

    public enum Status {
        WAITING,
        NOTIFIED,
        CLAIMED,
        EXPIRED,
        CANCELED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id")
    private Sala sala;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    private Usuario professor;

    @Column(name = "janela_inicio", nullable = false)
    private LocalDateTime janelaInicio;

    @Column(name = "janela_fim", nullable = false)
    private LocalDateTime janelaFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.WAITING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "notify_expires_at")
    private LocalDateTime notifyExpiresAt; // quando NOTIFIED, até quando pode dar claim

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sala getSala() { return sala; }
    public void setSala(Sala sala) { this.sala = sala; }
    public Usuario getProfessor() { return professor; }
    public void setProfessor(Usuario professor) { this.professor = professor; }
    public LocalDateTime getJanelaInicio() { return janelaInicio; }
    public void setJanelaInicio(LocalDateTime janelaInicio) { this.janelaInicio = janelaInicio; }
    public LocalDateTime getJanelaFim() { return janelaFim; }
    public void setJanelaFim(LocalDateTime janelaFim) { this.janelaFim = janelaFim; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getNotifyExpiresAt() { return notifyExpiresAt; }
    public void setNotifyExpiresAt(LocalDateTime notifyExpiresAt) { this.notifyExpiresAt = notifyExpiresAt; }
}
