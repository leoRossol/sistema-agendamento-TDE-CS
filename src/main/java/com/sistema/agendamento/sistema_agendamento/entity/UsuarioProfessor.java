package com.sistema.agendamento.sistema_agendamento.entity;

import java.util.List;

import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios_professores")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class UsuarioProfessor extends UsuarioFuncionario {

    @OneToMany(mappedBy = "professor", cascade = CascadeType.ALL)
    private List<Turma> turmasLecionadas;

    public UsuarioProfessor() {
        super();
    }

    public UsuarioProfessor(String nome, String email, String senha, String departamento, int matricula) {
        super(nome, email, senha, TipoUsuario.PROFESSOR, departamento, matricula);
    }

    public List<Turma> getTurmasLecionadas() { return turmasLecionadas; }
    public void setTurmasLecionadas(List<Turma> turmasLecionadas) { this.turmasLecionadas = turmasLecionadas; }
    public Boolean addTurmaLecionada(Turma turma) { return this.turmasLecionadas.add(turma); }
}

