package com.sistema.agendamento.sistema_agendamento.dto;

import java.time.LocalDateTime;

public class AgendaItemDTO {
    private final Long reservaId;
    private final Long turmaId;
    private final LocalDateTime inicio;
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
}
