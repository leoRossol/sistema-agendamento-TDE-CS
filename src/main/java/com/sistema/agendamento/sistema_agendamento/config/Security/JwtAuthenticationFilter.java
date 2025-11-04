package com.sistema.agendamento.sistema_agendamento.config.Security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.crypto.SecretKey;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                SecretKey chave = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(chave)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String email = claims.getSubject();
                String role = claims.get("tipo", String.class);
                if (role == null) {
                    throw new JwtException("Claim 'tipo' (Role) não encontrada no token.");
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JwtException e) {
                System.err.println("Falha na validação do Token: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
