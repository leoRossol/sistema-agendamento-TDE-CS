package com.sistema.agendamento.sistema_agendamento.controller.Usuario;

import com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin.RegistroRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin.RegistroResponseDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin.RemoverUsuarioRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin.RemoverUsuarioResponseDTO;
import com.sistema.agendamento.sistema_agendamento.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class UsuarioAdminController {

    @Autowired
    private UsuarioService usuarioService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/registrar-usuario")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        RegistroResponseDTO response = usuarioService.registrarUsuario(dto);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/deletar-usuario")
    public ResponseEntity<?> deletarUsuario(@RequestBody RemoverUsuarioRequestDTO dto) {
        RemoverUsuarioResponseDTO response = usuarioService.removerUsuario(dto);
        return ResponseEntity.ok(response);
    } 
}
