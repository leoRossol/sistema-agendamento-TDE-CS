package com.sistema.agendamento.sistema_agendamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class EventoResponseDTO {
    public Long id;
    
    @Schema(description = "Status do evento (AGENDADO, CONFIRMADO, CANCELADO)", example = "CONFIRMADO")
    public String status;
    
    @Schema(description = "Tipo do evento (AULA, PROVA, SEMINARIO, OUTROS)", example = "AULA")
    public String tipo;
    
    @Schema(description = "Título do evento", example = "Aula de Cálculo I")
    public String titulo;
    
    @Schema(description = "Descrição do evento", example = "Primeira aula do semestre")
    public String descricao;
    
    @Schema(description = "ID do professor", example = "1")
    public Long professorId;
    
    @Schema(description = "ID da turma", example = "1")
    public Long turmaId;
    
    @Schema(description = "Recurso (sala) onde o evento ocorre: { tipo: \"SALA\", id: salaId }")
    public Object recurso;
    
    @Schema(description = "Data e hora de início", example = "2025-10-27T19:00:00")
    public LocalDateTime inicio;
    
    @Schema(description = "Data e hora de fim", example = "2025-10-27T21:00:00")
    public LocalDateTime fim;
    
    @Schema(description = "Data de criação")
    public LocalDateTime createdAt;
    
    @Schema(description = "Data de última atualização")
    public LocalDateTime updatedAt;
}
