package com.sistema.agendamento.sistema_agendamento.dto.Usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequestDTO {
    private String email;
    private String senha;
}
