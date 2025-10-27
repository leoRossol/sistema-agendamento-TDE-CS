package com.sistema.agendamento.sistema_agendamento.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Entity
@Table(name = "sala_equipamento")
public class SalaEquipamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipamento_id", nullable = false)
    private Equipamento equipamento;
    
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Column(name = "quantidade")
    private Integer quantidade = 1;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public SalaEquipamento() {
        this.createdAt = LocalDateTime.now();
    }
    
    public SalaEquipamento(Sala sala, Equipamento equipamento, Integer quantidade) {
        this();
        this.sala = sala;
        this.equipamento = equipamento;
        this.quantidade = quantidade;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Sala getSala() { return sala; }
    public void setSala(Sala sala) { this.sala = sala; }
    
    public Equipamento getEquipamento() { return equipamento; }
    public void setEquipamento(Equipamento equipamento) { this.equipamento = equipamento; }
    
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "SalaEquipamento{" +
                "id=" + id +
                ", quantidade=" + quantidade +
                '}';
    }
}
