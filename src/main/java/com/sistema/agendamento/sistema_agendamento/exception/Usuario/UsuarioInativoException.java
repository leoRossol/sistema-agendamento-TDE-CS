package com.sistema.agendamento.sistema_agendamento.exception.Usuario;

public class UsuarioInativoException extends RuntimeException {
    public UsuarioInativoException(String message) {
        super(message);
    }

    public UsuarioInativoException() {
        super("Usuário inativo. Entre em contato com o suporte.");
    }
    
}
