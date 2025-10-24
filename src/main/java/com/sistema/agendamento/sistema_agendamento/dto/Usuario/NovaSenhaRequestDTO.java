package com.sistema.agendamento.sistema_agendamento.dto.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NovaSenhaRequestDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String senhaAntiga;

    @NotBlank
    @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres")
    private String novaSenha;
    
    public NovaSenhaRequestDTO(String email, String senhaAntiga, String novaSenha) {
        this.email = email;
        this.senhaAntiga = senhaAntiga;
        this.novaSenha = novaSenha;
    }

    public String getEmail() { return email; }
    public String getSenhaAntiga() { return senhaAntiga; }
    public String getNovaSenha() { return novaSenha; }
}
