package com.sistema.agendamento.sistema_agendamento.controller;

import com.sistema.agendamento.sistema_agendamento.dto.SalaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.SalaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.dto.SalaReservadaDTO;
import com.sistema.agendamento.sistema_agendamento.dto.AgendaItemDTO;
import com.sistema.agendamento.sistema_agendamento.service.SalaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/infra")
@RequiredArgsConstructor
public class InfraController {
    
    private final SalaService salaService;

    @PostMapping("/alocacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public SalaResponseDTO criar(@Valid @RequestBody SalaRequestDTO request){
        return salaService.alocar(request);
    }

    @GetMapping("/salas/{id}/agenda")
    public List<AgendaItemDTO> agenda(
        @PathVariable Long id,
        @RequestParam("periodo")
        @DateTimeFormat(pattern = "yyyy-MM") YearMonth periodo
    ){
        return salaService.agendaSalaNoMes(id, periodo);
    }

    @GetMapping("/reservas")
    public ResponseEntity<java.util.List<SalaReservadaDTO>> getSalasReservadas(
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime from,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime to
    ) {
        if ((from == null) != (to == null)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
            "Informe ambos os parametros from e to ou nenhum");
        }
        if (from == null && to != null && from.isAfter(to)){
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
            "From deve ser <= to");
        }
        return ResponseEntity.ok(salaService.listarSalasReservadas(from, to));
    }
}
