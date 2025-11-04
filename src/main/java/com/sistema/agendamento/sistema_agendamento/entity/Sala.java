package com.sistema.agendamento.sistema_agendamento.entity;


import com.sistema.agendamento.sistema_agendamento.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "salas")
@Getter
@Setter
@AllArgsConstructor
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
}
