package com.sistema.agendamento.sistema_agendamento.exception.Usuario;

public class MatriculaInvalidaException extends RuntimeException {
    public MatriculaInvalidaException(String message) {
        super(message);
    }

    public MatriculaInvalidaException() {
        super("A matricula já está sendo utilizada.");
    }
}
