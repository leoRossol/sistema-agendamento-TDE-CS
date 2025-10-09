package com.sistema.agendamento.sistema_agendamento.dto;

import java.time.LocalDateTime;

public class EventoResponseDTO {
    public Long id;
    public String status;
    public String tipo;
    public String titulo;
    public String descricao;
    public Long professorId;
    public Long turmaId;
    public Object recurso; // { tipo: "SALA", id: salaId }
    public LocalDateTime inicio;
    public LocalDateTime fim;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
