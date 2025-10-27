package com.sistema.agendamento.sistema_agendamento.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sistema.agendamento.sistema_agendamento.dto.CreateEventoRequest;
import com.sistema.agendamento.sistema_agendamento.dto.EventoResponse;
import com.sistema.agendamento.sistema_agendamento.entity.Evento;
import com.sistema.agendamento.sistema_agendamento.enums.StatusEventos;
import com.sistema.agendamento.sistema_agendamento.service.SchedulerService;
import com.sistema.agendamento.sistema_agendamento.service.SchedulerService.SchedulerConflict;

@RestController
@RequestMapping("/scheduler")
@Tag(name = "Scheduler", description = "API para agendamento e gerenciamento de eventos em salas/laboratórios")
public class SchedulerController {

    private final SchedulerService schedulerService;

    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/eventos")
    @Operation(
        summary = "Criar evento",
        description = "Cria um novo evento (aula, prova, seminário) em uma sala, bloqueando o slot na agenda e validando conflitos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Evento criado com sucesso e slot bloqueado na agenda da sala",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EventoResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos (horário inválido, campos obrigatórios faltando)"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito de horário (sala/professor/turma ocupados no período solicitado)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"code\":\"CONFLITO_AGENDA\",\"message\":\"Conflito detectado\",\"sugestoes\":[...]}")
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Validação falhou (professor, turma ou sala não encontrados)"
        )
    })
    public ResponseEntity<?> criar(
            @Parameter(description = "Dados do evento a ser criado", required = true)
            @Valid @RequestBody CreateEventoRequest body
    ) {
        Evento e = schedulerService.criarEvento(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(e));
    }

    @PutMapping("/eventos/{id}")
    @Operation(
        summary = "Atualizar evento",
        description = "Atualiza um evento existente, revalidando conflitos e bloqueando novo slot na agenda da sala"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Evento atualizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EventoResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Evento não encontrado"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito de horário detectado"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Validação falhou"
        )
    })
    public ResponseEntity<?> atualizar(
            @Parameter(description = "ID do evento a ser atualizado", required = true)
            @PathVariable("id") Long id,
            
            @Parameter(description = "Dados atualizados do evento", required = true)
            @Valid @RequestBody CreateEventoRequest body
    ) {
        Evento e = schedulerService.atualizarEvento(id, body);
        return ResponseEntity.ok(toResponse(e));
    }

    @GetMapping("/eventos/{id}")
    @Operation(
        summary = "Buscar evento por ID",
        description = "Retorna os detalhes de um evento específico pelo seu ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Evento encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EventoResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Evento não encontrado"
        )
    })
    public ResponseEntity<?> obter(
            @Parameter(description = "ID do evento") @PathVariable Long id
    ) {
        Evento e = schedulerService.obterEvento(id);
        return ResponseEntity.ok(toResponse(e));
    }

    @GetMapping("/calendario/professores/{id}")
    @Operation(
        summary = "Consultar calendário do professor",
        description = "Retorna a lista de eventos de um professor para um período específico (formato ISO-8601: inicio/fim)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Calendário retornado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EventoResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetro período inválido"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor não encontrado"
        )
    })
    public ResponseEntity<?> calendarioProfessor(
            @Parameter(description = "ID do professor")
            @PathVariable("id") Long professorId,
            
            @Parameter(description = "Período no formato início/fim (ISO-8601). Ex: 2025-10-01T00:00:00/2025-10-31T23:59:59")
            @RequestParam("periodo") String periodo
    ) {
        // Decode URL-encoded values. Some clients may double-encode, so decode until stable.
        String decoded = safeUrlDecode(periodo);
        String[] p = decoded.split("/");
        if (p.length != 2) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", "PARAMETRO_INVALIDO", "message", "periodo no formato inicio/fim é obrigatório"));
        }
        final LocalDateTime inicio;
        final LocalDateTime fim;
        try {
            inicio = LocalDateTime.parse(p[0]);
            fim = LocalDateTime.parse(p[1]);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", "PARAMETRO_INVALIDO", "message", "periodo inválido. Use ISO-8601: yyyy-MM-dd'T'HH:mm:ss/yyyy-MM-dd'T'HH:mm:ss"));
        }
        var eventos = schedulerService.calendarioProfessor(professorId, inicio, fim);
        var lista = eventos.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(lista);
    }

    private static String safeUrlDecode(String value) {
        String prev;
        String curr = value;
        // Decode up to 3 times to handle cases like %252F -> %2F -> /
        for (int i = 0; i < 3; i++) {
            prev = curr;
            curr = URLDecoder.decode(curr, StandardCharsets.UTF_8);
            if (curr.equals(prev)) break;
        }
        return curr;
    }

    private EventoResponse toResponse(Evento e) {
        EventoResponse r = new EventoResponse();
        r.id = e.getId();
        r.status = e.getStatus() == null ? StatusEventos.CONFIRMADO.name() : e.getStatus().name();
        r.tipo = e.getTipoEvento() != null ? e.getTipoEvento().name() : null;
        r.titulo = e.getTitulo();
        r.descricao = e.getDescricao();
        r.professorId = e.getProfessor() != null ? e.getProfessor().getId() : null;
        r.turmaId = e.getTurma() != null ? e.getTurma().getId() : null;
        r.recurso = e.getSala() != null ? Map.of("tipo", "SALA", "id", e.getSala().getId()) : null;
        r.inicio = e.getDataInicio();
        r.fim = e.getDataFim();
        r.createdAt = e.getCreatedAt();
        //r.updatedAt = e.getUpdatedAt();
        return r;
    }
}
