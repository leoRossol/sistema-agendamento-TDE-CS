package com.sistema.agendamento.sistema_agendamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Request DTO para criação de evento")
public class CreateEventoRequest {
    
    @Schema(description = "Título do evento", required = true, example = "Aula de Cálculo I")
    public String titulo;
    
    @Schema(description = "Descrição do evento", example = "Primeira aula do semestre")
    public String descricao;
    
    @Schema(description = "Tipo do evento: AULA, PROVA, SEMINARIO ou OUTROS", required = true, example = "AULA")
    public String tipoEvento;
    
    @Schema(description = "ID do professor que ministrará o evento", required = true, example = "1")
    public Long professorId;
    
    @Schema(description = "ID da turma (opcional)", example = "1")
    public Long turmaId;
    
    @Schema(description = "ID da sala/laboratório onde o evento ocorrerá", required = true, example = "10")
    public Long salaId;
    
    @Schema(description = "Data e hora de início do evento", required = true, example = "2025-10-27T19:00:00")
    public LocalDateTime inicio;
    
    @Schema(description = "Data e hora de fim do evento", required = true, example = "2025-10-27T21:00:00")
    public LocalDateTime fim;
}
