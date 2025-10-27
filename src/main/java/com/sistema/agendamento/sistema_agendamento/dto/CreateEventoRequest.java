package com.sistema.agendamento.sistema_agendamento.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request DTO para criação/atualização de evento")
public class CreateEventoRequest {
    
    @Schema(description = "Título do evento", required = true, example = "Aula de Cálculo I")
    @NotBlank(message = "Título é obrigatório")
    public String titulo;
    
    @Schema(description = "Descrição do evento", example = "Primeira aula do semestre")
    public String descricao;
    
    @Schema(description = "Tipo do evento", required = true, example = "AULA", allowableValues = {"AULA", "PROVA", "SEMINARIO", "OUTROS"})
    @NotBlank(message = "Tipo do evento é obrigatório")
    public String tipoEvento;
    
    @Schema(description = "ID do professor", required = true, example = "1")
    @NotNull(message = "ID do professor é obrigatório")
    public Long professorId;
    
    @Schema(description = "ID da turma (opcional)", example = "10")
    public Long turmaId;
    
    @Schema(description = "ID da sala/laboratório", required = true, example = "5")
    @NotNull(message = "ID da sala é obrigatório")
    public Long salaId;
    
    @Schema(description = "Data e hora de início do evento", required = true, example = "2025-10-01T08:00:00")
    @NotNull(message = "Data de início é obrigatória")
    public LocalDateTime inicio;
    
    @Schema(description = "Data e hora de fim do evento", required = true, example = "2025-10-01T10:00:00")
    @NotNull(message = "Data de fim é obrigatória")
    public LocalDateTime fim;
}
