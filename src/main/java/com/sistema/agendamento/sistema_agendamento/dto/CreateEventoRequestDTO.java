package com.sistema.agendamento.sistema_agendamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class CreateEventoRequestDTO {
    public String titulo;
    
    @Schema(description = "Descrição do evento", example = "Primeira aula do semestre")
    public String descricao;
    
    @Schema(description = "Tipo do evento: AULA, PROVA, SEMINARIO ou OUTROS", required = true, example = "AULA")
    public String tipoEvento;
    
    @Schema(description = "ID do professor que ministrará o evento", required = true, example = "1")
    public Long professorId;
    public Long turmaId;   // opcional
    public Long salaId;    // obrigatório na US-03 (sala/lab) — aqui vamos usar Sala
    // US-09: reserva de múltiplos labs em conjunto (tudo ou nada)
    public java.util.List<Long> labs; // opcional; quando presente com 2+ itens, ignora salaId e usa multi-reserva
    public LocalDateTime inicio;
    
    @Schema(description = "Data e hora de fim do evento", required = true, example = "2025-10-27T21:00:00")
    public LocalDateTime fim;
}
