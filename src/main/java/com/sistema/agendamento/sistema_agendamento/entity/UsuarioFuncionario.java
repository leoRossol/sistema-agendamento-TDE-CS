package com.sistema.agendamento.sistema_agendamento.entity;

import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
import com.sistema.agendamento.sistema_agendamento.utils.FuncionarioUtils;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios_funcionarios")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class UsuarioFuncionario extends Usuario {

    private String departamento;

    public UsuarioFuncionario() { 
        super();
    }

    protected UsuarioFuncionario(String nome, String email, String senha, TipoUsuario tipoUsuario, String departamento, int matricula) {
        super(nome, email, senha, tipoUsuario, matricula);
        this.departamento = departamento;
    }

    public static UsuarioFuncionario novoFuncionario(String nome, String email, String senha, String departamento, int matricula) {
        if (!FuncionarioUtils.validarCredenciais(email, matricula, departamento))
            throw new IllegalArgumentException("Dados inválidos para criação de funcionário.");

        return new UsuarioFuncionario(nome, email, senha, TipoUsuario.PROFESSOR, departamento, matricula);
    }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
}
