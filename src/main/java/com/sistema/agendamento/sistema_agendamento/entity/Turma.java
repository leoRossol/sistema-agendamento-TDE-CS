package com.sistema.agendamento.sistema_agendamento.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "turmas", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"codigo", "semestre", "ano"}))
public class Turma {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false, length = 20)
    private String codigo;
    
    @NotBlank
    @Column(nullable = false, length = 10)
    private String semestre;
    
    @NotNull
    @Column(nullable = false)
    private Integer ano;
    
    private Boolean ativo = true;
    
    // Relacionamentos
    @ManyToOne
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;
    
    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Usuario professor;
    
    @OneToMany(mappedBy = "turma")
    private List<Matricula> matriculas;
    
    @OneToMany(mappedBy = "turma")
    private List<Evento> eventos;

    @ManyToMany
    @JoinTable(
    name = "turma_alunos",
    joinColumns = @JoinColumn(name = "turma_id"),
    inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    
private List<Usuario> alunos = new ArrayList<>();
    
    // Construtores
    public Turma() {}
    
    public Turma(String codigo, String semestre, Integer ano, Disciplina disciplina, Usuario professor) {
        this.codigo = codigo;
        this.semestre = semestre;
        this.ano = ano;
        this.disciplina = disciplina;
        this.professor = professor;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }
    
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    
    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }
    
    public Usuario getProfessor() { return professor; }
    public void setProfessor(Usuario professor) { this.professor = professor; }
    
    public List<Matricula> getMatriculas() { return matriculas; }
    public void setMatriculas(List<Matricula> matriculas) { this.matriculas = matriculas; }
    
    public List<Evento> getEventos() { return eventos; }
    public void setEventos(List<Evento> eventos) { this.eventos = eventos; }
}