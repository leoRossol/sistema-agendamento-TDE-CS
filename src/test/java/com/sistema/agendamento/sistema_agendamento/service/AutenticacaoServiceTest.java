package com.sistema.agendamento.sistema_agendamento.service;
import com.sistema.agendamento.sistema_agendamento.dto.LoginRequestDTO;
import com.sistema.agendamento.sistema_agendamento.entity.AuthResponse;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AutenticacaoServiceTest {

    // Assuma que a constante está na classe Service
    private static final String LOGIN_FAILURE_MSG = "Falha na autenticação";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AutenticacaoService authService;

    private Usuario usuarioMock;
    private LoginRequestDTO loginRequestDTO;

    private final String EMAIL_VALIDO = "thiago@teste.com";
    private final String SENHA_CORRETA = "123456";
    private final String HASH_BCRYPT = "$2a$10$N9ZpL9L.OM.PmTjK.V.R.O.9Pz.Ea.F.9Qz.1a.B.2c.D.3e";
    private final String TOKEN_MOCK = "fake.jwt.token.123";

    @Test
    void loginValidoDeveRetornarAuthResponseComToken() {
        when(usuarioRepository.findByEmail(EMAIL_VALIDO))
                .thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches(SENHA_CORRETA, HASH_BCRYPT))
                .thenReturn(true);

        AuthResponse response = authService.login(loginRequestDTO);

        assertNotNull(response);
        assertEquals(EMAIL_VALIDO, response.getUsername());
        assertEquals(TOKEN_MOCK, response.getTokem());
    }

    @Test
    void senhaIncorretaDeveFalhar() {
        // Arrange
        LoginRequestDTO requestInvalido = new LoginRequestDTO(EMAIL_VALIDO, "senhaerrada");

        when(usuarioRepository.findByEmail(EMAIL_VALIDO))
                .thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("senhaerrada", HASH_BCRYPT))
                .thenReturn(false); // Mock da senha incorreta

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(requestInvalido));

        assertEquals(LOGIN_FAILURE_MSG, exception.getMessage());
    }

    @Test
    void senhaNaoBcryptNoBancoDeveFalhar() {
        // Arrange
        Usuario usuarioSemBcrypt = criarUsuarioMock(EMAIL_VALIDO, "senhaEmTextoPlano");

        when(usuarioRepository.findByEmail(EMAIL_VALIDO))
                .thenReturn(Optional.of(usuarioSemBcrypt));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(loginRequestDTO));

        assertEquals(LOGIN_FAILURE_MSG, exception.getMessage());
    }

    @Test
    void usuarioInexistenteDeveFalhar() {
        // Arrange
        LoginRequestDTO requestNaoExiste = new LoginRequestDTO("naoexiste@teste.com", SENHA_CORRETA);

        when(usuarioRepository.findByEmail("naoexiste@teste.com"))
                .thenReturn(Optional.empty()); // Mock do usuário não encontrado

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(requestNaoExiste));

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    private Usuario criarUsuarioMock(String email, String senha) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha(senha);
        return usuario;
    }
}
