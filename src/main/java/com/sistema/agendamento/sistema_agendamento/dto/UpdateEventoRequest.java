package com.sistema.agendamento.sistema_agendamento.dto;

import java.time.LocalDateTime;

/**
 * DTO minimalista para PATCH de eventos (edição/cancelamento) por professor dono do evento.
 * Para este escopo, usamos principalmente ownerId (professor autenticado) e status.
 */
public class UpdateEventoRequest {
    public Long ownerId; // professor autenticado (deve ser o dono do evento)
    public String status; // opcional; se == "CANCELADO" realiza cancelamento

    // Campos opcionais para edições leves (não obrigatórios neste US, mas previstos)
    public String titulo;
    public String descricao;
    public LocalDateTime inicio;
    public LocalDateTime fim;
    public Long salaId;
}
