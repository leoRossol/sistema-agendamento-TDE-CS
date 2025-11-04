package com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RemoverUsuarioRequestDTO {
    @NotNull
    @Min(1)
    public final Long idUsuario;

    public RemoverUsuarioRequestDTO(Long idUsuario) { this.idUsuario = idUsuario; }

    public Long getIdUsuario() { return idUsuario; }    
}
