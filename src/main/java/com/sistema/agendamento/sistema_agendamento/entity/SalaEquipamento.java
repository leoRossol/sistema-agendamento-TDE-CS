package com.sistema.agendamento.sistema_agendamento.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sala_equipamento")
@Getter
@Setter
@AllArgsConstructor
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

    @Override
    public String toString() {
        return "SalaEquipamento{" +
                "id=" + id +
                ", quantidade=" + quantidade +
                '}';
    }
}
