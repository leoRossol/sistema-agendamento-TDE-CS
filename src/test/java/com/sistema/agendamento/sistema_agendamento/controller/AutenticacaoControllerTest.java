package com.sistema.agendamento.sistema_agendamento.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.agendamento.sistema_agendamento.dto.LoginRequestDTO;
import com.sistema.agendamento.sistema_agendamento.entity.AuthResponse;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import com.sistema.agendamento.sistema_agendamento.service.AutenticacaoService;
import com.sistema.agendamento.sistema_agendamento.service.TokenService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AutenticacaoController.class, properties = {
    "jwt.secret=ABCDEFGHIJKLMNOPQRSTUVWXZY012345" 
})
public class AutenticacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AutenticacaoService authService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String LOGIN_FAILURE_MSG = "Invalid username or password";

    @Test
    void login_ComCredenciaisValidas_DeveRetornarOkComToken() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("user@test.com", "senha123");
        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail("user@test.com");
        // Um hash BCrypt válido de exemplo
        usuarioMock.setSenha("$2a$10$N9ZpL9L.OM.PmTjK.V.R.O.9Pz.Ea.F.9Qz.1a.B.2c.D.3e");

        when(usuarioRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(usuarioMock));

        when(passwordEncoder.matches("senha123", usuarioMock.getSenha()))
                .thenReturn(true);

        when(tokenService.gerarToken(usuarioMock))
                .thenReturn("fake.jwt.token");

        // Act & Assert
        mockMvc.perform(post("/auth/login") // Ajuste o endpoint se necessário
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user@test.com"))
                .andExpect(jsonPath("$.tokem").value("fake.jwt.token")); // "tokem" como no seu DTO
    }

    @Test
    void login_ComUsuarioInexistente_DeveRetornarErro() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("user@test.com", "senha123");

        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Uma RuntimeException não tratada geralmente vira 500 Internal Server Error
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(RuntimeException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("Usuário não encontrado", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void login_ComSenhaNaoBcrypt_DeveRetornarErro() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("user@test.com", "senha123");
        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail("user@test.com");
        usuarioMock.setSenha("senha_em_texto_plano_123"); // Senha não-BCrypt

        when(usuarioRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(usuarioMock));

        // O passwordEncoder.matches nem será chamado

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RuntimeException))
                .andExpect(result -> assertEquals(LOGIN_FAILURE_MSG, result.getResolvedException().getMessage()));
    }
}
