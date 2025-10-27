package com.sistema.agendamento.sistema_agendamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "DTO para item da agenda de uma sala")
public class AgendaItemDTO {
    
    @Schema(description = "ID da reserva", example = "1")
    private final Long reservaId;
    
    @Schema(description = "ID da turma alocada", example = "10")
    private final Long turmaId;
    
    @Schema(description = "Data e hora de início", example = "2025-10-01T08:00:00")
    private final LocalDateTime inicio;
    
    @Schema(description = "Data e hora de fim", example = "2025-10-01T10:00:00")
    private final LocalDateTime fim;

    public AgendaItemDTO(Long reservaId, Long turmaId, LocalDateTime inicio, LocalDateTime fim) {
        this.reservaId = reservaId;
        this.turmaId = turmaId;
        this.inicio = inicio;
        this.fim = fim;
    }

    public Long getReservaId() { return reservaId; }
    public Long getTurmaId() { return turmaId; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFim() { return fim; }

    // aliases no estilo record para compatibilidade com testes
    public Long reservaId() { return reservaId; }
    public Long turmaId() { return turmaId; }
    public LocalDateTime inicio() { return inicio; }
    public LocalDateTime fim() { return fim; }
}
