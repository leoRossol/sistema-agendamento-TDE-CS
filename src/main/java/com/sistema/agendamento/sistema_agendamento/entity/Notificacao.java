package com.sistema.agendamento.sistema_agendamento.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
@Data
@EqualsAndHashCode(callSuper = false)
public class Notificacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(nullable = false, length = 100)
    private String titulo;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacao tipo;
    
    @Column(columnDefinition = "boolean default false")
    private Boolean lida = false;
    
    @Column(name = "data_envio", columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private LocalDateTime dataEnvio = LocalDateTime.now();
    
    @Column(name = "data_leitura")
    private LocalDateTime dataLeitura;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    private Evento evento;
    
    public enum TipoNotificacao {
        AULA, ENTREGA, AVALIACAO, SALA_DISPONIVEL, GERAL
    }
}
