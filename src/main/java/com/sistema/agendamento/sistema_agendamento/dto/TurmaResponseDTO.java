package com.sistema.agendamento.sistema_agendamento.dto;

import com.sistema.agendamento.sistema_agendamento.entity.Turma;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurmaResponseDTO {
    
    private Long id;
    private String codigo;
    private String semestre;
    private Integer ano;
    private Boolean ativo;
    private DisciplinaSimplificadaDTO disciplina;
    private ProfessorSimplificadoDTO professor;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisciplinaSimplificadaDTO {
        private Long id;
        private String nome;
        private String codigo;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfessorSimplificadoDTO {
        private Long id;
        private String nome;
        private String email;
    }
    
    public static TurmaResponseDTO fromEntity(Turma turma) {
        TurmaResponseDTO dto = new TurmaResponseDTO();
        dto.setId(turma.getId());
        dto.setCodigo(turma.getCodigo());
        dto.setSemestre(turma.getSemestre());
        dto.setAno(turma.getAno());
        dto.setAtivo(turma.getAtivo());
        
        if (turma.getDisciplina() != null) {
            DisciplinaSimplificadaDTO disciplinaDTO = new DisciplinaSimplificadaDTO(
                turma.getDisciplina().getId(),
                turma.getDisciplina().getNome(),
                turma.getDisciplina().getCodigo()
            );
            dto.setDisciplina(disciplinaDTO);
        }
        
        if (turma.getProfessor() != null) {
            ProfessorSimplificadoDTO professorDTO = new ProfessorSimplificadoDTO(
                turma.getProfessor().getId(),
                turma.getProfessor().getNome(),
                turma.getProfessor().getEmail()
            );
            dto.setProfessor(professorDTO);
        }
        
        return dto;
    }
}

