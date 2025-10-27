package com.sistema.agendamento.sistema_agendamento.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuração do OpenAPI/Swagger para documentação da API.
 * Configurado especificamente para o microserviço scheduler-svc (US-04).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Agendamento - Scheduler API")
                        .description("API REST para agendamento de eventos (aulas, provas, seminários) em salas e laboratórios, com bloqueio automático de slots na agenda")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sistema Agendamento")
                                .email("contato@sistema.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Ambiente de Desenvolvimento")
                ));
    }
}

