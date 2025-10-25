package com.sistema.agendamento.sistema_agendamento.controller.Usuario;

import com.sistema.agendamento.sistema_agendamento.dto.Usuario.NovaSenhaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.NovaSenhaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PreAuthorize("hasAnyRole('ADMINISTADOR', 'PROFESSOR', 'COORDENADOR', 'ALUNO')")
    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody NovaSenhaRequestDTO dto) {
        NovaSenhaResponseDTO response = usuarioService.redefinirSenha(dto);
        return ResponseEntity.ok(response);
    }

    // endpoints especificos por role aqui
}
