package com.sistema.agendamento.sistema_agendamento.controller;

import com.sistema.agendamento.sistema_agendamento.dto.UsuarioRequestDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registrar_DeveRegistrarUsuarioComSucesso() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Thiago");
        dto.setEmail("thiago@email.com");
        dto.setSenha("123456");
        dto.setTipoUsuario(TipoUsuario.PROFESSOR);

        when(usuarioRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("senhaCriptografada");

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(1L);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        ResponseEntity<?> response = usuarioController.registrar(dto);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("ID: 1"));

        // verifica se o usuario foi salvo corretamente
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioCapturado = captor.getValue();
        assertEquals("Thiago", usuarioCapturado.getNome());
        assertEquals("thiago@email.com", usuarioCapturado.getEmail());
        assertEquals("senhaCriptografada", usuarioCapturado.getSenha());
        assertTrue(usuarioCapturado.isAtivo());
        assertEquals("ADMIN", usuarioCapturado.getTipoUsuario());
    }

    @Test
    void registrar_DeveRetornarErroQuandoEmailExistente() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setEmail("teste@email.com");

        when(usuarioRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(new Usuario()));

        ResponseEntity<?> response = usuarioController.registrar(dto);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("E-mail já está em uso", response.getBody());
        verify(usuarioRepository, never()).save(any());
    }
}
