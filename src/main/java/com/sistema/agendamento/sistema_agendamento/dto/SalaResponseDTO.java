package com.sistema.agendamento.sistema_agendamento.dto;

public class SalaResponseDTO {
    private final Long reservaId;
    private final boolean conflito;
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