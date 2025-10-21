package com.sistema.agendamento.sistema_agendamento.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
public class SchedulerController {

    private final SchedulerService schedulerService;

    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/eventos")
    public ResponseEntity<?> criar(@RequestBody CreateEventoRequest body) {
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

    @PutMapping("/eventos/{id}")
    public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody CreateEventoRequest body) {
        try {
            Evento e = schedulerService.atualizarEvento(id, body);
            return ResponseEntity.ok(toResponse(e));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", "NAO_ENCONTRADO", "message", "Evento não encontrado"));
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
    public ResponseEntity<?> obter(@PathVariable Long id) {
        try {
            Evento e = schedulerService.obterEvento(id);
            return ResponseEntity.ok(toResponse(e));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", "NAO_ENCONTRADO", "message", "Evento não encontrado"));
        }
    }

    @GetMapping("/calendario/professores/{id}")
    public ResponseEntity<?> calendarioProfessor(@PathVariable("id") Long professorId,
                                                 @RequestParam("periodo") String periodo) {
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
