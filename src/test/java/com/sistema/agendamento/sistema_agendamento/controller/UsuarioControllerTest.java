//package com.sistema.agendamento.sistema_agendamento.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sistema.agendamento.sistema_agendamento.controller.Usuario.UsuarioController;
//import com.sistema.agendamento.sistema_agendamento.dto.Usuario.NovaSenhaRequestDTO;
//import com.sistema.agendamento.sistema_agendamento.dto.Usuario.NovaSenhaResponseDTO;
//import com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin.RegistroRequestDTO;
//import com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin.RegistroResponseDTO;
//import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
//import com.sistema.agendamento.sistema_agendamento.service.UsuarioService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//public class UsuarioControllerTest {
//
//    private MockMvc mockMvc;
//
//    @Mock
//    private UsuarioService usuarioService;
//
//    @InjectMocks
//    private UsuarioController usuarioController;
//
//    private ObjectMapper objectMapper;
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
//        objectMapper = new ObjectMapper();
//    }
//
//    @Test
//    void registrarUsuarioDeveRetornar200() throws Exception {
//        RegistroRequestDTO registroRequest = new RegistroRequestDTO("Thiago", "thiago@email.com", "Senha@123", TipoUsuario.ALUNO, 222222222);
//
//        RegistroResponseDTO responseDTO = new RegistroResponseDTO();
//        responseDTO.setMensagem("Usuário registrado com sucesso!");
//        responseDTO.setId(1L);
//
//        when(usuarioService.registrarUsuario(any())).thenReturn(responseDTO);
//
//        mockMvc.perform(post("/usuarios/registrar")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(registroRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.mensagem").value("Usuário registrado com sucesso!"))
//                .andExpect(jsonPath("$.id").value(1));
//    }
//
//    @Test
//    void redefinirSenhaDeveRetornar200() throws Exception {
//        NovaSenhaRequestDTO novaSenhaRequest = new NovaSenhaRequestDTO("thiago@email.com", "Senha@123", "NovaSenha@1");
//
//        when(usuarioService.redefinirSenha(any())).thenReturn(new NovaSenhaResponseDTO() {{ setMensagem("Senha redefinida com sucesso!");}});
//
//        mockMvc.perform(post("/usuarios/redefinir-senha")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(novaSenhaRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.mensagem").value("Senha redefinida com sucesso!"));
//    }
//}
