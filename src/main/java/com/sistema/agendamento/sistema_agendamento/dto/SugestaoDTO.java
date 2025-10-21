package com.sistema.agendamento.sistema_agendamento.dto;

import java.time.LocalDateTime;

public class SugestaoDTO {
    public LocalDateTime inicio;
    public LocalDateTime fim;
    public String recursoTipo; // "SALA" para manter compatível
    public Long recursoId;     // sala alternativa
    public String motivo;

    public SugestaoDTO(LocalDateTime inicio, LocalDateTime fim, String recursoTipo, Long recursoId, String motivo) {
        this.inicio = inicio;
        this.fim = fim;
        this.recursoTipo = recursoTipo;
        this.recursoId = recursoId;
        this.motivo = motivo;
    }
}
