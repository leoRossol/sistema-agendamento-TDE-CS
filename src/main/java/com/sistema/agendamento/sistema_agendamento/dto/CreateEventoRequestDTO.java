package com.sistema.agendamento.sistema_agendamento.dto;

import java.time.LocalDateTime;

public class CreateEventoRequestDTO {
    public String titulo;
    public String descricao;
    public String tipoEvento; // AULA | PROVA | SEMINARIO | OUTROS (usa seu enum existente)
    public Long professorId;
    public Long turmaId;   // opcional
    public Long salaId;    // obrigatório na US-03 (sala/lab) — aqui vamos usar Sala
    // US-09: reserva de múltiplos labs em conjunto (tudo ou nada)
    public java.util.List<Long> labs; // opcional; quando presente com 2+ itens, ignora salaId e usa multi-reserva
    public LocalDateTime inicio;
    public LocalDateTime fim;
}
