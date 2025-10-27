package com.sistema.agendamento.sistema_agendamento.exception;

public class ProfessorInvalidoException extends RuntimeException {
    
    public ProfessorInvalidoException(String message) {
        super(message);
    }
    
    public ProfessorInvalidoException(Long professorId) {
        super("Professor inválido com ID: " + professorId);
    }
}

