package com.sistema.agendamento.sistema_agendamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SistemaAgendamentoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaAgendamentoApplication.class, args);
	}

}
