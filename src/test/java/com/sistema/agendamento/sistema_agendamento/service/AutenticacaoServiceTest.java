package com.sistema.agendamento.sistema_agendamento.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sistema.agendamento.sistema_agendamento.dto.Usuario.LoginRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.LoginResponseDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

public class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AutenticacaoService autenticacaoService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Usuario usuario;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        usuario = new Usuario("Thiago", "thiago@email.com", passwordEncoder.encode("Senha@123"), TipoUsuario.ALUNO, 222222222);
        usuario.setAtivo(true);
        usuario.setId(1L);
    }

    @Test
    void loginValidoDeveRetornarToken() {
        when(usuarioRepository.findByEmail("thiago@email.com")).thenReturn(Optional.of(usuario));

        LoginRequestDTO loginRequest = new LoginRequestDTO("thiago@email.com", "Senha@123");

        LoginResponseDTO response = autenticacaoService.login(loginRequest);

        assertNotNull(response.getToken());
        assertEquals(usuario.getId(), response.getId());
        assertEquals(usuario.getEmail(), response.getEmail());
    }

    @Test
    void loginComSenhaInvalidaDeveLancarException() {
        when(usuarioRepository.findByEmail("thiago@email.com")).thenReturn(Optional.of(usuario));

        LoginRequestDTO loginRequest = new LoginRequestDTO("thiago@email.com", "senhaErrada");

        Exception exception = assertThrows(RuntimeException.class, () -> { autenticacaoService.login(loginRequest); });
        assertEquals("Senha incorreta", exception.getMessage());
    }

    @Test
    void loginComUsuarioInexistenteDeveLancarException() {
        when(usuarioRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        LoginRequestDTO loginRequest = new LoginRequestDTO("naoexiste@email.com", "Senha@123");

        Exception exception = assertThrows(RuntimeException.class, () -> { autenticacaoService.login(loginRequest); });
        assertEquals("Usuário não encontrado", exception.getMessage());
    }
}
