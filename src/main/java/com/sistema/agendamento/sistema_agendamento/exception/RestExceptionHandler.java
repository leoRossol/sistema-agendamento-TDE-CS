package com.sistema.agendamento.sistema_agendamento.exception;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sistema.agendamento.sistema_agendamento.service.SchedulerService.SchedulerConflict;

@RestControllerAdvice
public class RestExceptionHandler {
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", ex.getMessage(), "conflito", true));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(SchedulerConflict.class)
    public ResponseEntity<Map<String, Object>> handleSchedulerConflict(SchedulerConflict ex) {
        Map<String, Object> error = Map.of(
                "code", ex.code,
                "message", ex.publicMessage,
                "sugestoes", ex.sugestoes.stream().map(s -> Map.of(
                        "inicio", s.inicio,
                        "fim", s.fim,
                        "recurso", Map.of("tipo", s.recursoTipo, "id", s.recursoId),
                        "motivo", s.motivo
                )).collect(Collectors.toList())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage()
                ))
                .collect(Collectors.toList());
        
        Map<String, Object> error = Map.of(
                "code", "ERRO_VALIDACAO",
                "errors", errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElementException(java.util.NoSuchElementException ex) {
        Map<String, Object> error = Map.of(
                "code", "NAO_ENCONTRADO",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}

