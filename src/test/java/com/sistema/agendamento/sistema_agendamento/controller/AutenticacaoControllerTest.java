package com.sistema.agendamento.sistema_agendamento.controller;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.service.AutenticacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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

    @MockBean
    private AutenticacaoService authService;

    @Test
    void loginComSucesso() throws Exception {
        Usuario u = new Usuario();
        u.setNome("Thiago");
        u.setEmail("thiago@teste.com");

        when(authService.login("thiago@teste.com", "123456")).thenReturn(u);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"thiago@teste.com\", \"senha\":\"123456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void loginSenhaIncorreta() throws Exception {
        when(authService.login("thiago@teste.com", "senhaerrada"))
            .thenThrow(new IllegalArgumentException("Senha incorreta"));

    mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"thiago@teste.com\", \"senha\":\"senhaerrada\"}"))
            .andExpect(status().is4xxClientError());
    }
}
