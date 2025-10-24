package com.sistema.agendamento.sistema_agendamento.exception.Usuario;

public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String message) {
        super(message);
    }

    public EmailJaCadastradoException() {
        super("O email fornecido já está cadastrado no sistema.");
    }
    
}
