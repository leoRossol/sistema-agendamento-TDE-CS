package com.sistema.agendamento.sistema_agendamento.exception;

public class TurmaNotFoundException extends RuntimeException {
    
    public TurmaNotFoundException(String message) {
        super(message);
    }
    
    public TurmaNotFoundException(Long turmaId) {
        super("Turma não encontrada com ID: " + turmaId);
    }
}

