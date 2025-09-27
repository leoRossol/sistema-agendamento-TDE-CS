package com.sistema.agendamento.sistema_agendamento.repository;

import com.sistema.agendamento.sistema_agendamento.entity.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    
    List<Disciplina> findByAtivoTrue();
    
    List<Disciplina> findByCodigoContainingIgnoreCase(String codigo);
    
    List<Disciplina> findByNomeContainingIgnoreCase(String nome);
}
