package com.sistema.agendamento.sistema_agendamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sistema.agendamento.sistema_agendamento.entity.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
}
