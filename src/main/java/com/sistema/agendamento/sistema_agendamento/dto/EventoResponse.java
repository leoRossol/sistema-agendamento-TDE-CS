package com.sistema.agendamento.sistema_agendamento.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response DTO para evento")
public class EventoResponse {
    @Schema(description = "ID do evento", example = "123")
    public Long id;
    
    @Schema(description = "Status do evento", example = "CONFIRMADO")
    public String status;
    
    @Schema(description = "Tipo do evento", example = "AULA")
    public String tipo;
    
    @Schema(description = "Título do evento", example = "Aula de Cálculo I")
    public String titulo;
    
    @Schema(description = "Descrição do evento", example = "Primeira aula do semestre")
    public String descricao;
    
    @Schema(description = "ID do professor", example = "1")
    public Long professorId;
    
    @Schema(description = "ID da turma", example = "10")
    public Long turmaId;
    
    @Schema(description = "Recurso alocado (sala)", example = "{\"tipo\":\"SALA\",\"id\":5}")
    public Object recurso;
    
    @Schema(description = "Data e hora de início", example = "2025-10-01T08:00:00")
    public LocalDateTime inicio;
    
    @Schema(description = "Data e hora de fim", example = "2025-10-01T10:00:00")
    public LocalDateTime fim;
    
    @Schema(description = "Data e hora de criação", example = "2025-10-01T07:00:00")
    public LocalDateTime createdAt;
    
    @Schema(description = "Data e hora de atualização", example = "2025-10-01T07:30:00")
    public LocalDateTime updatedAt;
}
