package com.sistema.agendamento.sistema_agendamento.entity;

import com.sistema.agendamento.sistema_agendamento.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
public class Evento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false, length = 200)
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @NotNull
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;
    
    @NotNull
    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;
    
    private Boolean recorrente = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    private TipoEvento tipoEvento;
    
    @Enumerated(EnumType.STRING)
    private StatusEventos status = StatusEventos.AGENDADO;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relacionamentos
    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Usuario professor;
    
    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;
    
    @ManyToOne
    @JoinColumn(name = "sala_id")
    private Sala sala;
    
    // Construtores
    public Evento() {}
    
    public Evento(String titulo, LocalDateTime dataInicio, LocalDateTime dataFim, 
                  TipoEvento tipoEvento, Usuario professor, Turma turma, Sala sala) {
        this.titulo = titulo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.tipoEvento = tipoEvento;
        this.professor = professor;
        this.turma = turma;
        this.sala = sala;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }
    
    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }
    
    public Boolean getRecorrente() { return recorrente; }
    public void setRecorrente(Boolean recorrente) { this.recorrente = recorrente; }
    
    public TipoEvento getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(TipoEvento tipoEvento) { this.tipoEvento = tipoEvento; }
    
    public StatusEventos getStatus() { return status; }
    public void setStatus(StatusEventos status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Usuario getProfessor() { return professor; }
    public void setProfessor(Usuario professor) { this.professor = professor; }
    
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
    
    public Sala getSala() { return sala; }
    public void setSala(Sala sala) { this.sala = sala; }
}