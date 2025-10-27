package com.sistema.agendamento.sistema_agendamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response DTO para alocação de sala")
public class SalaResponseDTO {
    
    @Schema(description = "ID da reserva criada", example = "123")
    private final Long reservaId;
    
    @Schema(description = "Indica se houve conflito", example = "false")
    private final boolean conflito;
    
    @Schema(description = "Mensagem de resposta", example = "Alocação criada")
    private final String mensagem;

    public SalaResponseDTO(Long reservaId, boolean conflito, String mensagem) {
        this.reservaId = reservaId;
        this.conflito = conflito;
        this.mensagem = mensagem;
    }

    // getters padrão (Jackson)
    public Long getReservaId() { return reservaId; }
    public boolean isConflito() { return conflito; }
    public String getMensagem() { return mensagem; }

    // aliases no estilo record (compatibilidade com chamadas resp.reservaId())
    public Long reservaId() { return reservaId; }
    public boolean conflito() { return conflito; }
    public String mensagem() { return mensagem; }
}