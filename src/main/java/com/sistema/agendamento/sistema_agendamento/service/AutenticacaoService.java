package com.sistema.agendamento.sistema_agendamento.service;
import com.sistema.agendamento.sistema_agendamento.dto.LoginRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.UserDTO;
import com.sistema.agendamento.sistema_agendamento.entity.AuthResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AutenticacaoService {

    private static final String LOGIN_FAILURE_MSG = "Invalid username or password";
    private static final String SIGNUP_FAILURE_MSG = "This user already exists";

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // armazenamos somente o encoder, nao a senha em si
    private final TokenService tokenService;

    public AuthResponse login(LoginRequestDTO loginRequestDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String stored = usuario.getSenha();
        boolean isBcrypt = stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
        boolean authenticated;
        if (isBcrypt & passwordEncoder.matches(loginRequestDTO.getSenha(), stored)) {
            String token = this.tokenService.gerarToken(usuario);
            return new AuthResponse(loginRequestDTO.getEmail(), token);
        }
        else throw new RuntimeException(LOGIN_FAILURE_MSG);
    }

    public AuthResponse signup(UserDTO userDTO) {
        Optional<Usuario> usuario = this.usuarioRepository.findByEmail(userDTO.getEmail());

        if (usuario.isEmpty()) {
            var newUser = toEntity(userDTO);
            this.usuarioRepository.save(newUser);

            return new AuthResponse(userDTO.getEmail(), "User Created");
        }
        else throw new RuntimeException(SIGNUP_FAILURE_MSG);
    }

    private Usuario toEntity(UserDTO userDTO) {
        return Usuario.builder()
                .nome(userDTO.getUsername())
                .email(userDTO.getEmail())
                .senha(userDTO.getPassword())
                .tipoUsuario(userDTO.getUserType())
                .senha(passwordEncoder.encode(userDTO.getPassword()))
                .build();
    }


}