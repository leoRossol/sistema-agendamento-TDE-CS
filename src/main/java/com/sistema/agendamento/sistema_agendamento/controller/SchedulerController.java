package com.sistema.agendamento.sistema_agendamento.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sistema.agendamento.sistema_agendamento.dto.CreateEventoRequest;
import com.sistema.agendamento.sistema_agendamento.dto.EventoResponse;
import com.sistema.agendamento.sistema_agendamento.dto.UpdateEventoRequest;
import com.sistema.agendamento.sistema_agendamento.dto.WaitlistRequest;
import com.sistema.agendamento.sistema_agendamento.dto.WaitlistResponse;
import com.sistema.agendamento.sistema_agendamento.entity.Evento;
import com.sistema.agendamento.sistema_agendamento.enums.StatusEventos;
import com.sistema.agendamento.sistema_agendamento.service.SchedulerService;
import com.sistema.agendamento.sistema_agendamento.service.SchedulerService.SchedulerConflict;

@RestController
@RequestMapping("/scheduler")
@Tag(name = "Scheduler", description = "API para agendamento de eventos em salas/laboratórios")
public class SchedulerController {

    private final SchedulerService schedulerService;

    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/eventos")
    @Operation(
        summary = "Criar evento",
        description = "Cria um novo evento (aula, prova, seminário) em uma sala, validando conflitos de horário com sala, professor e turma"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Evento criado com sucesso",
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
                schema = @Schema(example = """
                    {
                        "code": "CONFLITO_AGENDA",
                        "message": "Conflito detectado com recurso/professor/turma.",
                        "sugestoes": [
                            {
                                "inicio": "2025-10-27T19:10:00",
                                "fim": "2025-10-27T21:10:00",
                                "recurso": { "tipo": "SALA", "id": 2 },
                                "motivo": "Outro recurso no mesmo horário"
                            }
                        ]
                    }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Validação falhou (professor, turma ou sala não encontrados)"
        )
    })
    public ResponseEntity<?> criar(
            @Parameter(description = "Dados do evento a ser criado", required = true)
            @RequestBody CreateEventoRequest body
    ) {
        try {
            Evento e = schedulerService.criarEvento(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(e));
        } catch (IllegalArgumentException ex) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("code", "ERRO_VALIDACAO");
            err.put("errors", List.of(Map.of("message", ex.getMessage())));
            return ResponseEntity.unprocessableEntity().body(err);
        } catch (SchedulerConflict ex) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("code", ex.code);
            err.put("message", ex.publicMessage);
            err.put("sugestoes", ex.sugestoes.stream().map(s -> Map.of(
                    "inicio", s.inicio,
                    "fim", s.fim,
                    "recurso", Map.of("tipo", s.recursoTipo, "id", s.recursoId),
                    "motivo", s.motivo
            )).toList());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
        }
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
        try {
            Evento e = schedulerService.obterEvento(id);
            return ResponseEntity.ok(toResponse(e));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", "NAO_ENCONTRADO", "message", "Evento não encontrado"));
        }
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
        @RequestParam("periodo") String periodo,

        @Parameter(description = "Formato opcional de saída. Use 'ical' para iCalendar")
        @RequestParam(value = "format", required = false) String format,

        @RequestHeader(value = "Accept", required = false) String accept
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

        boolean wantsIcal = (format != null && "ical".equalsIgnoreCase(format))
                || (accept != null && accept.toLowerCase().contains("text/calendar"));
        if (wantsIcal) {
            String ics = gerarICal(eventos);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/calendar; charset=UTF-8")
                    .body(ics);
        }

        var lista = eventos.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/calendario/alunos/{id}/agora")
    @Operation(
        summary = "Aula atual e próxima do aluno",
        description = "Retorna a aula em andamento e a próxima, se houver, para o aluno informado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada"),
        @ApiResponse(responseCode = "422", description = "alunoId inválido")
    })
    public ResponseEntity<?> aulaAtualEProxima(
            @Parameter(description = "ID do aluno") @PathVariable("id") Long alunoId
    ) {
        try {
            var result = schedulerService.aulaAtualEProximaDoAluno(alunoId, java.time.LocalDateTime.now());
            var lista = result.stream().map(this::toResponse).toList();
            return ResponseEntity.ok(lista);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.unprocessableEntity().body(Map.of("code", "ERRO_VALIDACAO", "message", ex.getMessage()));
        }
    }

    @PatchMapping("/eventos/{id}")
    @Operation(
        summary = "Gerenciar evento (PATCH)",
        description = "Edição leve ou cancelamento por professor dono do evento"
    )
    public ResponseEntity<?> patchEvento(@PathVariable("id") Long id, @RequestBody UpdateEventoRequest body) {
        try {
            Evento e = schedulerService.patchEvento(id, body);
            return ResponseEntity.ok(toResponse(e));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code", "NAO_ENCONTRADO", "message", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.unprocessableEntity().body(Map.of("code", "ERRO_VALIDACAO", "message", ex.getMessage()));
        }
    }

    @PostMapping("/waitlist")
    @Operation(
        summary = "Entrar na lista de espera (lab)",
        description = "Professor entra na lista de espera para um laboratório e recebe a posição inicial"
    )
    public ResponseEntity<?> entrarWaitlist(@RequestBody WaitlistRequest req) {
        try {
            var res = schedulerService.entrarNaWaitlist(req.labId, req.professorId, req.inicio, req.fim);
            WaitlistResponse out = new WaitlistResponse();
            out.id = res.id();
            out.position = res.position();
            return ResponseEntity.status(HttpStatus.CREATED).body(out);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.unprocessableEntity().body(Map.of("code", "ERRO_VALIDACAO", "message", ex.getMessage()));
        }
    }

    @PostMapping("/waitlist/{id}/claim")
    @Operation(
        summary = "Confirmar reserva liberada (claim)",
        description = "Professor notificado confirma a reserva na janela informada"
    )
    public ResponseEntity<?> claimWaitlist(@PathVariable("id") Long entryId,
                                           @RequestParam(value = "professorId", required = false) Long professorId) {
        try {
            boolean ok = schedulerService.claimWaitlist(entryId, professorId);
            return ResponseEntity.ok(Map.of("claimed", ok));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code", "NAO_ENCONTRADO", "message", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.unprocessableEntity().body(Map.of("code", "ERRO_VALIDACAO", "message", ex.getMessage()));
        }
    }

    @GetMapping("/aulas")
    @Operation(
        summary = "Aulas do dia",
        description = "Retorna todas as aulas do dia informado (parâmetro data: yyyy-MM-dd)"
    )
    public ResponseEntity<?> aulasDoDia(@RequestParam("data") String data) {
        try {
            LocalDate d = LocalDate.parse(data);
            var eventos = schedulerService.aulasDoDia(d);
            var lista = eventos.stream().map(this::toResponse).toList();
            return ResponseEntity.ok(lista);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("code", "PARAMETRO_INVALIDO", "message", "data inválida (yyyy-MM-dd)"));
        }
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

    private String gerarICal(List<Evento> eventos) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\n");
        sb.append("VERSION:2.0\n");
        sb.append("PRODID:-//sistema-agendamento//PT-BR\n");
        for (Evento e : eventos) {
            sb.append("BEGIN:VEVENT\n");
            // Simplificação: datas locais sem timezone (para testes basta conter os campos)
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
            if (e.getDataInicio() != null) sb.append("DTSTART:").append(e.getDataInicio().format(fmt)).append("\n");
            if (e.getDataFim() != null) sb.append("DTEND:").append(e.getDataFim().format(fmt)).append("\n");
            if (e.getTitulo() != null) sb.append("SUMMARY:").append(e.getTitulo()).append("\n");
            if (e.getDescricao() != null) sb.append("DESCRIPTION:").append(e.getDescricao()).append("\n");
            if (e.getSala() != null) sb.append("LOCATION:").append(e.getSala().getNome()).append("\n");
            sb.append("END:VEVENT\n");
        }
        sb.append("END:VCALENDAR\n");
        return sb.toString();
    }
}
