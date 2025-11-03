package com.sistema.agendamento.sistema_agendamento.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String username;
    private String tokem;
}
