package com.sistema.agendamento.sistema_agendamento.controller;

import com.sistema.agendamento.sistema_agendamento.dto.RelatorioOcupacaoRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.RelatorioOcupacaoResponseDTO;
import com.sistema.agendamento.sistema_agendamento.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
@Tag(name = "Relatórios", description = "API para geração de relatórios de ocupação e estatísticas")
public class ReportsController {

    private final ReportsService reportsService;

    @PreAuthorize("hasAnyRole('ADMINISTADOR', 'COORDENADOR')")
    @GetMapping("/ocupacao")
    @Operation(
        summary = "Gerar relatório de ocupação",
        description = "Gera relatório detalhado de ocupação de salas por curso, disciplina e período acadêmico. " +
                     "Permite filtrar por curso, disciplina ou sala específica para análises focadas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Relatório gerado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RelatorioOcupacaoResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros inválidos - período deve estar no formato YYYY/S (ex: 2025/2)"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Erro de validação nos parâmetros de entrada"
        )
    })
    public ResponseEntity<RelatorioOcupacaoResponseDTO> gerarRelatorioOcupacao(
            @Parameter(
                description = "Período acadêmico no formato YYYY/S (ex: 2025/2)", 
                example = "2025/2",
                required = true
            )
            @RequestParam("periodo") 
            @NotBlank(message = "Período é obrigatório")
            @Pattern(regexp = "^\\d{4}/[12]$", message = "Período deve estar no formato YYYY/S (ex: 2025/2)")
            String periodo,
            
            @Parameter(description = "ID do curso para filtrar (opcional)", example = "1")
            @RequestParam(value = "cursoId", required = false) 
            Long cursoId,
            
            @Parameter(description = "ID da disciplina para filtrar (opcional)", example = "1")
            @RequestParam(value = "disciplinaId", required = false) 
            Long disciplinaId,
            
            @Parameter(description = "ID da sala para filtrar (opcional)", example = "1")
            @RequestParam(value = "salaId", required = false) 
            Long salaId) {

        RelatorioOcupacaoRequestDTO request = new RelatorioOcupacaoRequestDTO();
        request.setPeriodo(periodo);
        request.setCursoId(cursoId);
        request.setDisciplinaId(disciplinaId);
        request.setSalaId(salaId);

        RelatorioOcupacaoResponseDTO response = reportsService.gerarRelatorioOcupacao(request);
        return ResponseEntity.ok(response);
    }
}
