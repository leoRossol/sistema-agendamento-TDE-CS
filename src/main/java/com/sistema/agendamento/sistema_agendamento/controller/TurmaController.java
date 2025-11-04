package com.sistema.agendamento.sistema_agendamento.controller;

import com.sistema.agendamento.sistema_agendamento.dto.TurmaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.TurmaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.service.TurmaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog/turmas")
@RequiredArgsConstructor
@Tag(name = "Turmas", description = "API para gerenciamento de turmas")
public class TurmaController {
    
    private final TurmaService turmaService;

    @PreAuthorize("hasAnyRole('ADMINISTADOR', 'COORDENADOR')")
    @PostMapping
    @Operation(
        summary = "Criar uma nova turma",
        description = "Cria uma nova turma vinculada a uma disciplina e professor para um período específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Turma criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TurmaResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos no payload"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Código da turma já existe para o período especificado"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Disciplina ou professor inválido"
        )
    })
    public ResponseEntity<TurmaResponseDTO> criarTurma(
            @Valid @RequestBody TurmaRequestDTO request) {
        
        TurmaResponseDTO response = turmaService.criarTurma(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar turma por ID",
        description = "Retorna os detalhes de uma turma específica pelo seu ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Turma encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TurmaResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Turma não encontrada"
        )
    })
    public ResponseEntity<TurmaResponseDTO> buscarTurmaPorId(
            @Parameter(description = "ID da turma") @PathVariable Long id) {
        
        TurmaResponseDTO response = turmaService.buscarTurmaPorId(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(
        summary = "Listar turmas com filtros",
        description = "Lista todas as turmas, podendo filtrar por período e/ou professor"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de turmas retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TurmaResponseDTO.class)
            )
        )
    })
    public ResponseEntity<List<TurmaResponseDTO>> listarTurmas(
            @Parameter(description = "Período no formato YYYY/S (ex: 2025/2)")
            @RequestParam(required = false) String periodo,
            
            @Parameter(description = "ID do professor")
            @RequestParam(required = false) Long professorId) {
        
        List<TurmaResponseDTO> response = turmaService.buscarTurmas(periodo, professorId);
        return ResponseEntity.ok(response);
    }
}

