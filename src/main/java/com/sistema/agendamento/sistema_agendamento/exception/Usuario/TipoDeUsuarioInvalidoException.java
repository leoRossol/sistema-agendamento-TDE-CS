package com.sistema.agendamento.sistema_agendamento.exception.Usuario;

public class TipoDeUsuarioInvalidoException extends RuntimeException {
    public TipoDeUsuarioInvalidoException(String message) {
        super(message);
    }

    public TipoDeUsuarioInvalidoException() {
        super("Tipo de usuário inválido.");
    }
    
}
