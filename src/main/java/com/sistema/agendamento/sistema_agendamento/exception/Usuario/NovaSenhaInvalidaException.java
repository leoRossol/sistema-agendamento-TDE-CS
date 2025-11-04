package com.sistema.agendamento.sistema_agendamento.exception.Usuario;

public class NovaSenhaInvalidaException extends RuntimeException {
    public NovaSenhaInvalidaException(String message) {
        super(message);
    }

    public NovaSenhaInvalidaException() {
        super("A nova senha não atende aos critérios de segurança.");
    }
}
