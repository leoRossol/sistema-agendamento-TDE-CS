package com.sistema.agendamento.sistema_agendamento.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class AutenticacaoService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // armazenamos somente o encoder, nao a senha em si

    public Usuario login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String stored = usuario.getSenha();
        boolean isBcrypt = stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
        boolean authenticated;
        if (isBcrypt) {
            authenticated = passwordEncoder.matches(senha, stored);
        } else {
            authenticated = senha != null && senha.equals(stored);
        }
        if (!authenticated) throw new RuntimeException("Senha incorreta");
        return usuario;
    }
}