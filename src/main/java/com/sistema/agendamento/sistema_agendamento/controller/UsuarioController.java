package com.sistema.agendamento.sistema_agendamento.controller;

import com.sistema.agendamento.sistema_agendamento.dto.Usuario.NovaSenhaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.NovaSenhaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.RegistroRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.RegistroResponseDTO;
import com.sistema.agendamento.sistema_agendamento.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        RegistroResponseDTO response = usuarioService.registrarUsuario(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody NovaSenhaRequestDTO dto) {
        NovaSenhaResponseDTO response = usuarioService.redefinirSenha(dto);
        return ResponseEntity.ok(response);
    }
}
