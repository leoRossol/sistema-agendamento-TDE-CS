package com.sistema.agendamento.sistema_agendamento.exception.Usuario;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException(String message) {
        super(message);
    }

    public CredenciaisInvalidasException() {
        super("Credenciais inválidas. Verifique seu email e senha.");
    }
}
