package com.sistema.agendamento.sistema_agendamento.service;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AutenticacaoServiceTest {

    @Autowired
    private AutenticacaoService authService;

    @Test
    void loginValidoDeveFuncionar() {
        Usuario usuario = authService.login("thiago@teste.com", "123456");
        assertNotNull(usuario);
        assertEquals("Thiago", usuario.getNome());
    }

    @Test
    void senhaIncorretaDeveFalhar() {
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.login("thiago@teste.com", "senhaerrada"));
        assertEquals("Senha incorreta", exception.getMessage());
    }

    @Test
    void usuarioInexistenteDeveFalhar() {
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.login("naoexiste@teste.com", "123456"));
        assertEquals("Usuário não encontrado", exception.getMessage());
    }
}
