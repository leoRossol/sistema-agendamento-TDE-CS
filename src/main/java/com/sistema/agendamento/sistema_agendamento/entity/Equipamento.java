package com.sistema.agendamento.sistema_agendamento.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "equipamento")
public class Equipamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;
    
    @Size(max = 50, message = "Tipo deve ter no máximo 50 caracteres")
    @Column(name = "tipo", length = 50)
    private String tipo;
    
    @Column(name = "codigo", unique = true, length = 50)
    private String codigo;
    
    @Column(name = "disponivel")
    private Boolean disponivel = true;
    
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "equipamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<SalaEquipamento> salaEquipamentos = new HashSet<>();
    
    public Equipamento() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Equipamento(String nome, String tipo, String codigo) {
        this();
        this.nome = nome;
        this.tipo = tipo;
        this.codigo = codigo;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public Boolean getDisponivel() { return disponivel; }
    public void setDisponivel(Boolean disponivel) { this.disponivel = disponivel; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Set<SalaEquipamento> getSalaEquipamentos() { return salaEquipamentos; }
    public void setSalaEquipamentos(Set<SalaEquipamento> salaEquipamentos) { this.salaEquipamentos = salaEquipamentos; }
    
    @Override
    public String toString() {
        return "Equipamento{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", tipo='" + tipo + '\'' +
                ", codigo='" + codigo + '\'' +
                ", disponivel=" + disponivel +
                '}';
    }
}
