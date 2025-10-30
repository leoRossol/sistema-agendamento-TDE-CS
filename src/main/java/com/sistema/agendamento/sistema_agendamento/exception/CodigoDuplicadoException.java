package com.sistema.agendamento.sistema_agendamento.exception;

public class CodigoDuplicadoException extends RuntimeException {
    
    public CodigoDuplicadoException(String message) {
        super(message);
    }
    
    public CodigoDuplicadoException(String codigo, String semestre, Integer ano) {
        super("Código " + codigo + " já existe para o período " + semestre + "/" + ano);
    }
}

