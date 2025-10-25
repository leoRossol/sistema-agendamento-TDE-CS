package com.sistema.agendamento.sistema_agendamento.dto.Usuario;

public class LoginRequestDTO {
    private final String email;
    private final String senha;

    public LoginRequestDTO(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public String getEmail() { return email; }
    public String getSenha() { return senha; }
}
