package com.sistema.agendamento.sistema_agendamento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.LoginRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.LoginResponseDTO;
import com.sistema.agendamento.sistema_agendamento.service.AutenticacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AutenticacaoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AutenticacaoService autenticacaoService;

    @InjectMocks
    private AutenticacaoController autenticacaoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(autenticacaoController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void loginValidoDeveRetornarToken() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("thiago@email.com");
        loginRequest.setSenha("Senha@123");

        LoginResponseDTO loginResponse = new LoginResponseDTO();
        loginResponse.setMensagem("Login realizado com sucesso!");
        loginResponse.setId(1L);
        loginResponse.setToken("fake-jwt-token");
        
        when(autenticacaoService.login(any())).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("thiago@email.com"))
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }
}
