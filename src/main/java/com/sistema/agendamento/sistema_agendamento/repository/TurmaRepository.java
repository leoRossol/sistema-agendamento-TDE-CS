package com.sistema.agendamento.sistema_agendamento.repository;

import com.sistema.agendamento.sistema_agendamento.entity.Turma;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.entity.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {
    
    List<Turma> findByAtivoTrue();
    
    List<Turma> findByProfessor(Usuario professor);
    
    List<Turma> findByDisciplina(Disciplina disciplina);
    
    List<Turma> findBySemestreAndAno(String semestre, Integer ano);
}