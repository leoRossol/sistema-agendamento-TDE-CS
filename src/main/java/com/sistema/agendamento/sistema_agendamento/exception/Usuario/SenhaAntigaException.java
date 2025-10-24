package com.sistema.agendamento.sistema_agendamento.exception.Usuario;

public class SenhaAntigaException extends RuntimeException {
    public SenhaAntigaException(String message) {
        super(message);
    }

    public SenhaAntigaException() {
        super("A senha antiga fornecida está incorreta.");
    }
    
}
