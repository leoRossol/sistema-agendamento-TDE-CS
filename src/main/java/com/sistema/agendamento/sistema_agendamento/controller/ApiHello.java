package com.sistema.agendamento.sistema_agendamento.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiHello {
    @GetMapping("/api/hello")
    public String hello() {
        return "Backend funcionando!!";
    }
}
