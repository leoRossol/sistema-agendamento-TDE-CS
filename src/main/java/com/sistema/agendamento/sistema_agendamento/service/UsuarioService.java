package com.sistema.agendamento.sistema_agendamento.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Usuario register(String nome, String email, String senha, String tipoUsuario) {
        if (usuarioRepository.findByEmail(email).isPresent()) throw new RuntimeException("Email já cadastrado");

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(nome);
        novoUsuario.setEmail(email);
        novoUsuario.setSenha(passwordEncoder.encode(senha)); // senha criptografada
        novoUsuario.setTipoUsuario(TipoUsuario.PROFESSOR); // TO DO: desmockar 
        novoUsuario.setAtivo(true);

        if (!NovaSenhaValida(senha)) throw new RuntimeException("Senha não atende aos requisitos de segurança");

        return usuarioRepository.save(novoUsuario);
    }

    public Boolean RedefinirSenha(String email, String senhaAntiga, String novaSenha) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(senhaAntiga, usuario.getSenha())) throw new RuntimeException("Senha antiga incorreta");

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        return true;
    }

    public Boolean NovaSenhaValida(String senha) {
        return senha != null 
        && senha.length() >= 6              
        && senha.length() <= 20 
        && senha.matches(".*\\d.*") // pelo menos um digito
        && senha.matches(".*[A-Z].*") // pelo menos uma letra maiuscula
        && senha.matches(".*[a-z].*") // pelo menos uma letra minuscula
        && senha.matches(".*[!@#$%^&*()].*"); // pelo menos um caractere especial
    }
}