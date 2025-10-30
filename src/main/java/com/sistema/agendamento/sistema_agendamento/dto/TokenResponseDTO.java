package com.sistema.agendamento.sistema_agendamento.dto;

public class TokenResponseDTO {
    private String token;

    public TokenResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
}
