package com.sistema.agendamento.sistema_agendamento.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para sugestão de horários alternativos quando há conflito")
public class SugestaoDTO {
    @Schema(description = "Data e hora de início sugerida", example = "2025-10-01T08:10:00")
    public LocalDateTime inicio;
    
    @Schema(description = "Data e hora de fim sugerida", example = "2025-10-01T10:10:00")
    public LocalDateTime fim;
    
    @Schema(description = "Tipo do recurso", example = "SALA")
    public String recursoTipo;
    
    @Schema(description = "ID do recurso sugerido", example = "6")
    public Long recursoId;
    
    @Schema(description = "Motivo da sugestão", example = "Outro recurso no mesmo horário")
    public String motivo;

    public SugestaoDTO(LocalDateTime inicio, LocalDateTime fim, String recursoTipo, Long recursoId, String motivo) {
        this.inicio = inicio;
        this.fim = fim;
        this.recursoTipo = recursoTipo;
        this.recursoId = recursoId;
        this.motivo = motivo;
    }
}
