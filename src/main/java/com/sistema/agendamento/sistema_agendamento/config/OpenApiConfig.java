package com.sistema.agendamento.sistema_agendamento.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Agendamento - API")
                        .description("API REST para sistema de agendamento de salas, eventos, relatórios de ocupação e alocação de infraestrutura")
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

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("sistema-agendamento")
                .pathsToMatch("/reports/**", "/infra/**", "/catalog/**", "/scheduler/**")
                .build();
    }
}
