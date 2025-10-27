package com.sistema.agendamento.sistema_agendamento.entity;


import com.sistema.agendamento.sistema_agendamento.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "salas")
public class Sala {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false, length = 50)
    private String nome;
    
    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String numero;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sala", nullable = false)
    private TipoSala tipoSala;
    
    @NotNull
    @Column(nullable = false)
    private Integer capacidade;
    
    @Column(name = "eh_conjunto")
    private Boolean ehConjunto = false;
    
    private Boolean ativo = true;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    // Relacionamentos
    @OneToMany(mappedBy = "sala")
    private List<Evento> eventos;
    
    @OneToMany(mappedBy = "sala", cascade = CascadeType.ALL)
    private Set<SalaEquipamento> equipamentos;
    
    @ManyToMany
    @JoinTable(
        name = "salas_conjuntas",
        joinColumns = @JoinColumn(name = "sala_principal_id"),
        inverseJoinColumns = @JoinColumn(name = "sala_secundaria_id")
    )
    private Set<Sala> salasConjuntas;
    
    // Construtores
    public Sala() {}
    
    public Sala(String nome, String numero, TipoSala tipoSala, Integer capacidade) {
        this.nome = nome;
        this.numero = numero;
        this.tipoSala = tipoSala;
        this.capacidade = capacidade;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    public TipoSala getTipoSala() { return tipoSala; }
    public void setTipoSala(TipoSala tipoSala) { this.tipoSala = tipoSala; }
    
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
    
    public Boolean getEhConjunto() { return ehConjunto; }
    public void setEhConjunto(Boolean ehConjunto) { this.ehConjunto = ehConjunto; }
    
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public List<Evento> getEventos() { return eventos; }
    public void setEventos(List<Evento> eventos) { this.eventos = eventos; }
    
    public Set<SalaEquipamento> getEquipamentos() { return equipamentos; }
    public void setEquipamentos(Set<SalaEquipamento> equipamentos) { this.equipamentos = equipamentos; }
    
    public Set<Sala> getSalasConjuntas() { return salasConjuntas; }
    public void setSalasConjuntas(Set<Sala> salasConjuntas) { this.salasConjuntas = salasConjuntas; }
}
