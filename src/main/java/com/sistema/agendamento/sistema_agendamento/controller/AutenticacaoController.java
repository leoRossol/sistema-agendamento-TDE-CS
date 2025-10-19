package com.sistema.agendamento.sistema_agendamento.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sistema.agendamento.sistema_agendamento.dto.LoginRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.TokenResponseDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.service.AutenticacaoService;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    @Autowired
    private AutenticacaoService autenticacaoService;
    @Value("${jwt.secret}")
    private String jwtSecret; 
    private static final long EXPIRACAO = 1000 * 60 * 60; // 1 hora

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        Usuario usuario = autenticacaoService.login(dto.getEmail(), dto.getSenha());
        String token = gerarToken(usuario);
        return ResponseEntity.ok(new TokenResponseDTO(token));
    }

    private String gerarToken(Usuario usuario) {
        SecretKey chave = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(usuario.getEmail()) // identificacao principal do token: email
                .claim("tipo", usuario.getTipoUsuario()) // claims adicionais
                .claim("id", usuario.getId())
                .setIssuedAt(new Date()) // data de emissao
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRACAO))
                .signWith(chave) // assinatura com chave secreta
                .compact(); // finaliza e gera o token
    }
}
