package com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin;

import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistroRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    private final String nome;

    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    private final String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres")
    private final String senha;

    private final TipoUsuario tipoUsuario;

    @Digits(integer = 8, fraction = 0, message = "A matrícula deve ter exatamente 8 dígitos")
    private final int matricula;

    public RegistroRequestDTO(String nome, String email, String senha, TipoUsuario tipoUsuario, int matricula) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
        this.matricula = matricula;
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public int getMatricula() { return matricula; }
}
