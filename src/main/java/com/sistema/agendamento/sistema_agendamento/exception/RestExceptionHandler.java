package com.sistema.agendamento.sistema_agendamento.exception;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sistema.agendamento.sistema_agendamento.service.SchedulerService.SchedulerConflict;

@RestControllerAdvice
public class RestExceptionHandler {
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(SchedulerConflict.class)
    public ResponseEntity<Map<String, Object>> handleSchedulerConflict(SchedulerConflict ex) {
        Map<String, Object> body = Map.of(
            "status", HttpStatus.CONFLICT.value(),
            "message", ex.publicMessage,
            "sugestoes", ex.sugestoes.stream().map(s -> Map.of(
                "inicio", s.inicio,
                "fim", s.fim,
                "recurso", Map.of("tipo", s.recursoTipo, "id", s.recursoId),
                "motivo", s.motivo
            )).collect(Collectors.toList())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", defaultMessage(error)
                ))
                .collect(Collectors.toList());

        Map<String, Object> body = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", "Erro de validação",
                "validationErrors", errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
            .map(v -> Map.of(
                "field", extractParamName(v),
                "message", v.getMessage()
            )).collect(Collectors.toList());

        Map<String, Object> body = Map.of(
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "message", "Erro de validação",
            "validationErrors", errors
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String extractParamName(ConstraintViolation<?> v) {
        // propertyPath like 'gerarRelatorioOcupacao.periodo' -> take last node
        String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
        int idx = path.lastIndexOf('.');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    @ExceptionHandler(TurmaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTurmaNotFound(TurmaNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({DisciplinaInvalidaException.class, ProfessorInvalidoException.class})
    public ResponseEntity<Map<String, Object>> handleUnprocessableEntity(RuntimeException ex) {
        return buildError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(CodigoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicated(CodigoDuplicadoException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElementException(java.util.NoSuchElementException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private Map<String, Object> errorBody(HttpStatus status, String message) {
        return Map.of(
            "status", status.value(),
            "message", message
        );
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(errorBody(status, message));
    }

    private String defaultMessage(FieldError error) {
        return error.getDefaultMessage() != null ? error.getDefaultMessage() : (error.getField() + " inválido");
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

