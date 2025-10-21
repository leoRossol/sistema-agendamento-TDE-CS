package com.sistema.agendamento.sistema_agendamento.controller;

import com.sistema.agendamento.sistema_agendamento.dto.SalaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.SalaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.dto.AgendaItemDTO;
import com.sistema.agendamento.sistema_agendamento.service.SalaService;
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
}
