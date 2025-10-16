package com.sistema.agendamento.sistema_agendamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurmaRequestDTO {
    
    @NotBlank(message = "Código é obrigatório")
    private String codigo;
    
    @NotBlank(message = "Semestre é obrigatório")
    private String semestre;
    
    @NotNull(message = "Ano é obrigatório")
    private Integer ano;
    
    @NotNull(message = "ID da disciplina é obrigatório")
    private Long disciplinaId;
    
    @NotNull(message = "ID do professor é obrigatório")
    private Long professorId;
}

