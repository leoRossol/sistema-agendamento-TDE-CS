package com.sistema.agendamento.sistema_agendamento.exception;

public class DisciplinaInvalidaException extends RuntimeException {
    
    public DisciplinaInvalidaException(String message) {
        super(message);
    }
    
    public DisciplinaInvalidaException(Long disciplinaId) {
        super("Disciplina inválida com ID: " + disciplinaId);
    }
}

