package com.sistema.agendamento.sistema_agendamento.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sistema.agendamento.sistema_agendamento.dto.Usuario.NovaSenhaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.RegistroRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.RegistroResponseDTO;
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

public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Usuario usuario;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        usuario = new Usuario("Thiago", "thiago@email.com", passwordEncoder.encode("Senha@123"), TipoUsuario.ALUNO);
        usuario.setAtivo(true);
        usuario.setId(1L);
    }

    @Test
    void registrarUsuarioValidoDeveRetornarSucesso() {
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        RegistroRequestDTO registroRequest = new RegistroRequestDTO("Thiago", "thiago@email.com", "Senha@123", TipoUsuario.ALUNO);
        RegistroResponseDTO response = usuarioService.registrarUsuario(registroRequest);

        assertEquals("Usuário registrado com sucesso!", response.getMensagem());
        assertEquals(usuario.getId(), response.getId());
    }

    @Test
    void redefinirSenhaValidaDeveAtualizarSenha() {
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        NovaSenhaRequestDTO novaSenhaRequest = new NovaSenhaRequestDTO(usuario.getEmail(), "Senha@123", "NovaSenha@1");

        var response = usuarioService.redefinirSenha(novaSenhaRequest);

        assertEquals("Senha redefinida com sucesso!", response.getMensagem());
        assertTrue(passwordEncoder.matches("NovaSenha@1", usuario.getSenha()));
    }
}
