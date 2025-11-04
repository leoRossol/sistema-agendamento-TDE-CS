package com.sistema.agendamento.sistema_agendamento.utils;

import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;

// utilitario para professores/coordenadores/admin
public class FuncionarioUtils {
    private static boolean isEmailFuncionario(String email, int matricula) {
        return email != null 
            && email.endsWith("@pucrs.br")
            && email.startsWith(Integer.toString(matricula));
    }

    private static boolean isMatriculaFuncionario(int matricula) {
        return Integer.toString(matricula).length() >= 8
            && Integer.toString(matricula).startsWith("100");
    }

    public static boolean validarCredenciais(String email, int matricula, String departamento) {
        return isEmailFuncionario(email, matricula)
            && isMatriculaFuncionario(matricula)
            && departamento == TipoUsuario.ADMINISTRADOR.toString()
            || departamento == TipoUsuario.COORDENACAO.toString()
            || departamento == TipoUsuario.PROFESSOR.toString();
    }
}