package com.sistema.agendamento.sistema_agendamento.exception.Usuario;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(String message) {
        super(message);
    }

    public UsuarioNaoEncontradoException() {
        super("Usuário não encontrado.");
    }
    
}
