package com.sistema.agendamento.sistema_agendamento.enums;

public enum TipoUsuario {
    ADMINISTRADOR,
    PROFESSOR,
    ALUNO,
    COORDENACAO;

@Override
public String toString() {
    switch (this) {
        case ADMINISTRADOR:
            return "Administrador";
        case PROFESSOR:
            return "Professor";
        case ALUNO:
            return "Aluno";
        case COORDENACAO:
            return "Coordenação";
        default:
            return "Desconhecido";
        }
    }
}