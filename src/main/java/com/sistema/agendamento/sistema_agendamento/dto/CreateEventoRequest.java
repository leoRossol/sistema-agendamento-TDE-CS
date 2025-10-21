package com.sistema.agendamento.sistema_agendamento.dto;

import java.time.LocalDateTime;

public class CreateEventoRequest {
    public String titulo;
    public String descricao;
    public String tipoEvento; // AULA | PROVA | SEMINARIO | OUTROS (usa seu enum existente)
    public Long professorId;
    public Long turmaId;   // opcional
    public Long salaId;    // obrigatório na US-03 (sala/lab) — aqui vamos usar Sala
    public LocalDateTime inicio;
    public LocalDateTime fim;
}
