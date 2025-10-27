package com.sistema.agendamento.sistema_agendamento.service;

import com.sistema.agendamento.sistema_agendamento.dto.TurmaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.TurmaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Disciplina;
import com.sistema.agendamento.sistema_agendamento.entity.Turma;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
import com.sistema.agendamento.sistema_agendamento.exception.CodigoDuplicadoException;
import com.sistema.agendamento.sistema_agendamento.exception.DisciplinaInvalidaException;
import com.sistema.agendamento.sistema_agendamento.exception.ProfessorInvalidoException;
import com.sistema.agendamento.sistema_agendamento.exception.TurmaNotFoundException;
import com.sistema.agendamento.sistema_agendamento.repository.DisciplinaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.TurmaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurmaService {
    
    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final UsuarioRepository usuarioRepository;
    
    @Transactional
    public TurmaResponseDTO criarTurma(TurmaRequestDTO dto) {
        // Validar se disciplina existe
        Disciplina disciplina = disciplinaRepository.findById(dto.getDisciplinaId())
            .orElseThrow(() -> new DisciplinaInvalidaException(dto.getDisciplinaId()));
        
        // Validar se professor existe e é do tipo PROFESSOR
        Usuario professor = usuarioRepository.findById(dto.getProfessorId())
            .orElseThrow(() -> new ProfessorInvalidoException(dto.getProfessorId()));
        
        if (professor.getTipoUsuario() != TipoUsuario.PROFESSOR) {
            throw new ProfessorInvalidoException("Usuário com ID " + dto.getProfessorId() + " não é um professor");
        }
        
        // Validar código único no período
        if (turmaRepository.existsByCodigoAndSemestreAndAno(dto.getCodigo(), dto.getSemestre(), dto.getAno())) {
            throw new CodigoDuplicadoException(dto.getCodigo(), dto.getSemestre(), dto.getAno());
        }
        
        // Criar e salvar turma
        Turma turma = new Turma();
        turma.setCodigo(dto.getCodigo());
        turma.setSemestre(dto.getSemestre());
        turma.setAno(dto.getAno());
        turma.setDisciplina(disciplina);
        turma.setProfessor(professor);
        turma.setAtivo(true);
        
        Turma turmaSalva = turmaRepository.save(turma);
        
        return TurmaResponseDTO.fromEntity(turmaSalva);
    }
    
    @Transactional(readOnly = true)
    public TurmaResponseDTO buscarTurmaPorId(Long id) {
        Turma turma = turmaRepository.findById(id)
            .orElseThrow(() -> new TurmaNotFoundException(id));
        
        return TurmaResponseDTO.fromEntity(turma);
    }
    
    @Transactional(readOnly = true)
    public List<TurmaResponseDTO> buscarTurmas(String periodo, Long professorId) {
        List<Turma> turmas;
        
        // Se período e professorId fornecidos
        if (periodo != null && professorId != null) {
            String[] partes = periodo.split("/");
            String semestre = partes[1];
            Integer ano = Integer.parseInt(partes[0]);
            turmas = turmaRepository.findBySemestreAndAnoAndProfessorId(semestre, ano, professorId);
        }
        // Se apenas período fornecido
        else if (periodo != null) {
            String[] partes = periodo.split("/");
            String semestre = partes[1];
            Integer ano = Integer.parseInt(partes[0]);
            turmas = turmaRepository.findBySemestreAndAno(semestre, ano);
        }
        // Se apenas professorId fornecido
        else if (professorId != null) {
            Usuario professor = usuarioRepository.findById(professorId)
                .orElseThrow(() -> new ProfessorInvalidoException(professorId));
            turmas = turmaRepository.findByProfessor(professor);
        }
        // Se nenhum filtro fornecido
        else {
            turmas = turmaRepository.findAll();
        }
        
        return turmas.stream()
            .map(TurmaResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }
}

