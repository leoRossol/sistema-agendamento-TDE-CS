package com.sistema.agendamento.sistema_agendamento.service;

import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;
    private static final long EXPIRACAO = 1000 * 60 * 60; // 1 hora

    public String gerarToken(Usuario usuario) {
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
