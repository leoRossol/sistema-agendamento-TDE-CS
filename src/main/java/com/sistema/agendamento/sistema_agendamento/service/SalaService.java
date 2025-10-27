package com.sistema.agendamento.sistema_agendamento.service;

import com.sistema.agendamento.sistema_agendamento.dto.SalaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.SalaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.dto.AgendaItemDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Sala;
import com.sistema.agendamento.sistema_agendamento.entity.SalaEquipamento;
import com.sistema.agendamento.sistema_agendamento.entity.Turma;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.entity.ReservaSala;
import com.sistema.agendamento.sistema_agendamento.exception.ConflictException;
import com.sistema.agendamento.sistema_agendamento.repository.ReservaSalaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.SalaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.TurmaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.MatriculaRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.YearMonth;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SalaService {

    private final ReservaSalaRepository reservaSalaRepository;
    private final TurmaRepository turmaRepository;
    private final SalaRepository salaRepository;
    private final MatriculaRepository matriculaRepository;

    @Transactional
    public SalaResponseDTO alocar(SalaRequestDTO req) {
        // validar horário
        if (req.fim().isBefore(req.inicio()) || req.fim().isEqual(req.inicio())) {
            throw new IllegalArgumentException("Horário inválido: fim deve ser após o início");
        }

        Turma turma = turmaRepository.findById(req.turmaId()).orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));
        Sala sala  = salaRepository.findById(req.salaId()).orElseThrow(() -> new IllegalArgumentException("Sala não encontrada"));

        // conflito de horario
        boolean conflito = reservaSalaRepository.temConflito(sala.getId(), req.inicio(), req.fim());
        if (conflito) {
            throw new ConflictException("Sala já alocada neste horário");
        }

        // turma sem professor
        Usuario professor = turma.getProfessor();
        if (professor == null){
            throw new IllegalArgumentException("Turma sem professor");
        }

        // capacidade e equipamentos
        validarCapacidade(sala, turma);

        Map<Long,Integer> requisitos = req.equipamentos() != null && !req.equipamentos().isEmpty()
            ? req.equipamentos()
            : requisitosDaTurma(turma);
        validarEquipamentos(sala, requisitos);

        // persistir
        ReservaSala reserva = new ReservaSala();
        reserva.setSala(sala);
        reserva.setTurma(turma);
        reserva.setProfessor(professor);
        reserva.setDataInicio(req.inicio());
        reserva.setDataFim(req.fim());

        ReservaSala salva = reservaSalaRepository.save(reserva);
        return new SalaResponseDTO(salva.getId(), false, "Alocação criada");
    }

    @Transactional(readOnly = true)
    public List<AgendaItemDTO> agendaSalaNoMes(Long salaId, YearMonth periodo) {
        var inicio = periodo.atDay(1).atStartOfDay();
        var fim = periodo.atEndOfMonth().atTime(23, 59, 59);
        
        return reservaSalaRepository.findaBySalaIdAndInicioBetween(salaId, inicio, fim)
            .stream()
            .map(r -> new AgendaItemDTO(
                r.getId(), 
                r.getTurma() != null ? r.getTurma().getId() : null,
                r.getDataInicio(), 
                r.getDataFim()
                ))
            .toList();
    }

    // ====== VALIDACOES =======

    private void validarCapacidade(Sala sala, Turma turma){
        long alunos = matriculaRepository.countByTurmaId(turma.getId());

        if (sala.getCapacidade() != null && sala.getCapacidade() < alunos) {
            throw new ConflictException("Capacidade insuficiente: sala=" + sala.getCapacidade() + ", turma=" + alunos);
        }
    }

    private void validarEquipamentos(Sala sala, Map<Long, Integer> requisitos) {
        if (requisitos == null || requisitos.isEmpty()) return;

        Map<Long, Integer> disponivel = new HashMap<>();
        if (sala.getEquipamentos() != null) {
            for (SalaEquipamento se : sala.getEquipamentos()) {
                Long eqId = se.getEquipamento().getId();
                disponivel.merge(eqId, se.getQuantidade() == null ? 0 : se.getQuantidade(), Integer::sum);
            }
        }

        // verificar faltas de equipamento
        StringBuilder faltas = new StringBuilder();
        requisitos.forEach((eqId, qtdNecessaria) -> {
            int qtd = disponivel.getOrDefault(eqId, 0);
            if (qtd < qtdNecessaria) {
                if (!faltas.isEmpty()) faltas.append(", ");
                faltas.append("equipamento ").append(eqId).append(" x").append(qtdNecessaria)
                    .append(" (disponivel =").append(qtd).append(")");
            }
        });

        if (faltas.length() > 0) {
            throw new ConflictException("Equipamentos insufucientes: " + faltas);
        }
    }

    private Map<Long, Integer> requisitosDaTurma (Turma turma) {
        return Collections.emptyMap();
    }
}
