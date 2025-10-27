package com.sistema.agendamento.sistema_agendamento.controller;

import com.sistema.agendamento.sistema_agendamento.dto.SalaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.SalaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.dto.AgendaItemDTO;
import com.sistema.agendamento.sistema_agendamento.service.SalaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/infra")
@RequiredArgsConstructor
@Tag(name = "Infraestrutura", description = "API para alocação de salas e consulta de agenda")
public class InfraController {
    
    private final SalaService salaService;

    @PostMapping("/alocacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Alocar sala para turma",
        description = "Cria uma alocação de sala para uma turma, validando capacidade, equipamentos e conflitos de horário"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Alocação criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SalaResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos (horário inválido, turma/sala não encontrada, sem professor)"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito de horário ou capacidade/equipamentos insuficientes"
        )
    })
    public SalaResponseDTO criar(
            @Parameter(description = "Dados da alocação: turmaId, salaId, horário início/fim e equipamentos")
            @Valid @RequestBody SalaRequestDTO request
    ){
        return salaService.alocar(request);
    }

    @GetMapping("/salas/{id}/agenda")
    @Operation(
        summary = "Consultar agenda de uma sala",
        description = "Retorna a agenda de alocações de uma sala para um período específico (mês/ano)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Agenda retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AgendaItemDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Período inválido"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sala não encontrada"
        )
    })
    public List<AgendaItemDTO> agenda(
        @Parameter(description = "ID da sala")
        @PathVariable Long id,
        
        @Parameter(description = "Período no formato yyyy-MM (ex: 2025-10)")
        @RequestParam("periodo")
        @DateTimeFormat(pattern = "yyyy-MM") YearMonth periodo
    ){
        return salaService.agendaSalaNoMes(id, periodo);
    }
}
