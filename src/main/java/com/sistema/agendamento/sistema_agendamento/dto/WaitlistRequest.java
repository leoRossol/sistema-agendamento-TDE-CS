package com.sistema.agendamento.sistema_agendamento.dto;

import java.time.LocalDateTime;

public class WaitlistRequest {
    public Long professorId;
    public Long labId;
    public LocalDateTime inicio;
    public LocalDateTime fim;
}
