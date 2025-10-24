package com.sistema.agendamento.sistema_agendamento.service;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sistema.agendamento.sistema_agendamento.dto.Usuario.LoginRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.LoginResponseDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;

import com.sistema.agendamento.sistema_agendamento.exception.Usuario.CredenciaisInvalidasException;
import com.sistema.agendamento.sistema_agendamento.exception.Usuario.UsuarioInativoException;
import com.sistema.agendamento.sistema_agendamento.exception.Usuario.UsuarioNaoEncontradoException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class AutenticacaoService {

    @Value("${jwt.secret}")
    private String jwtSecret; 
    private static final long ExpiracaoToken = 1000 * 60 * 60; // validade do token = 1h

    @Autowired
    private UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // armazenamos somente o encoder, nao a senha em si

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new UsuarioNaoEncontradoException());

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) 
            throw new CredenciaisInvalidasException();

        if (usuario.isAtivo() == false)
            throw new UsuarioInativoException();

        LoginResponseDTO response = new LoginResponseDTO();
        response.setId(usuario.getId());
        response.setEmail(usuario.getEmail());
        
        String token = gerarToken(response);

        if (token != null) {
            response.setToken(token);
            response.setMensagem("Login realizado com sucesso!");
        }

        return response;
    }
    
    private String gerarToken(LoginResponseDTO dto) {
    SecretKey chave = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
            .setSubject(dto.getEmail()) // identificacao principal do token: email
            .claim("id", dto.getId())
            .setIssuedAt(new Date()) // data de emissao
            .setExpiration(new Date(System.currentTimeMillis() + ExpiracaoToken))
            .signWith(chave) // assinatura com chave secreta
            .compact(); // finaliza e gera o token
    }
}
