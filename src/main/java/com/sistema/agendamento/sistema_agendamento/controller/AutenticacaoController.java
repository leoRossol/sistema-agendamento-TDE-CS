package com.sistema.agendamento.sistema_agendamento.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.sistema.agendamento.sistema_agendamento.dto.Usuario.LoginRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.LoginResponseDTO;
import com.sistema.agendamento.sistema_agendamento.service.AutenticacaoService;

@RestController
@RequestMapping("/autenticacao")
public class AutenticacaoController {

    @Autowired
    private AutenticacaoService autenticacaoService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = autenticacaoService.login(dto);
        return ResponseEntity.ok(response);
    }
}
