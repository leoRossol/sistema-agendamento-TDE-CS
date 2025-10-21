package com.sistema.agendamento.sistema_agendamento.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sistema.agendamento.sistema_agendamento.dto.CreateEventoRequest;
import com.sistema.agendamento.sistema_agendamento.dto.SugestaoDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Evento;
import com.sistema.agendamento.sistema_agendamento.entity.Sala;
import com.sistema.agendamento.sistema_agendamento.entity.Turma;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.enums.StatusEventos;
import com.sistema.agendamento.sistema_agendamento.enums.TipoEvento;
import com.sistema.agendamento.sistema_agendamento.repository.EventoRepository;
import com.sistema.agendamento.sistema_agendamento.repository.SalaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.TurmaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;

@Service
public class SchedulerService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final SalaRepository salaRepository;

    public SchedulerService(EventoRepository eventoRepository,
                            UsuarioRepository usuarioRepository,
                            TurmaRepository turmaRepository,
                            SalaRepository salaRepository) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.salaRepository = salaRepository;
    }

    public Evento criarEvento(CreateEventoRequest req) {
        validar(req);

        Usuario professor = usuarioRepository.findById(req.professorId)
                .orElseThrow(() -> new IllegalArgumentException("professorId não encontrado"));

        Turma turma = null;
        if (req.turmaId != null) {
            turma = turmaRepository.findById(req.turmaId)
                    .orElseThrow(() -> new IllegalArgumentException("turmaId não encontrado"));
        }

        Sala sala = salaRepository.findById(req.salaId)
                .orElseThrow(() -> new IllegalArgumentException("salaId não encontrado"));

        boolean conflitoSala = !eventoRepository.findConflitosAgendamento(sala, req.inicio, req.fim).isEmpty();
        boolean conflitoProfessor = !eventoRepository.findConflitosProfessor(professor, req.inicio, req.fim).isEmpty();
        boolean conflitoTurma = (turma != null) && !eventoRepository.findConflitosTurma(turma, req.inicio, req.fim).isEmpty();

        if (conflitoSala || conflitoProfessor || conflitoTurma) {
            List<SugestaoDTO> sug = sugerir(req, sala);
            throw new SchedulerConflict("CONFLITO_AGENDA", "Conflito detectado com recurso/professor/turma.", sug);
        }

        Evento e = new Evento();
        e.setTitulo(req.titulo);
        e.setDescricao(req.descricao);
        e.setDataInicio(req.inicio);
        e.setDataFim(req.fim);
        e.setTipoEvento(TipoEvento.valueOf(req.tipoEvento)); // usa o enum do projeto
        e.setProfessor(professor);
        e.setTurma(turma);
        e.setSala(sala);
        e.setStatus(StatusEventos.CONFIRMADO);

        return eventoRepository.save(e);
    }

    public Evento obterEvento(Long id) {
        return eventoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Evento não encontrado"));
    }

    public Evento atualizarEvento(Long id, CreateEventoRequest req) {
        validar(req);

        Evento existente = eventoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evento não encontrado"));

        Usuario professor = usuarioRepository.findById(req.professorId)
                .orElseThrow(() -> new IllegalArgumentException("professorId não encontrado"));

        Turma turma = null;
        if (req.turmaId != null) {
            turma = turmaRepository.findById(req.turmaId)
                    .orElseThrow(() -> new IllegalArgumentException("turmaId não encontrado"));
        }

        Sala sala = salaRepository.findById(req.salaId)
                .orElseThrow(() -> new IllegalArgumentException("salaId não encontrado"));

        boolean conflitoSala = !eventoRepository.findConflitosAgendamentoExceptId(sala, req.inicio, req.fim, id).isEmpty();
        boolean conflitoProfessor = !eventoRepository.findConflitosProfessorExceptId(professor, req.inicio, req.fim, id).isEmpty();
        boolean conflitoTurma = (turma != null) && !eventoRepository.findConflitosTurmaExceptId(turma, req.inicio, req.fim, id).isEmpty();

        if (conflitoSala || conflitoProfessor || conflitoTurma) {
            List<SugestaoDTO> sug = sugerir(req, sala);
            throw new SchedulerConflict("CONFLITO_AGENDA", "Conflito detectado com recurso/professor/turma.", sug);
        }

        existente.setTitulo(req.titulo);
        existente.setDescricao(req.descricao);
        existente.setDataInicio(req.inicio);
        existente.setDataFim(req.fim);
        existente.setTipoEvento(TipoEvento.valueOf(req.tipoEvento));
        existente.setProfessor(professor);
        existente.setTurma(turma);
        existente.setSala(sala);
        if (existente.getStatus() == null) {
            existente.setStatus(StatusEventos.CONFIRMADO);
        }

        return eventoRepository.save(existente);
    }

    public List<Evento> calendarioProfessor(Long professorId, LocalDateTime inicio, LocalDateTime fim) {
        Usuario professor = usuarioRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("professorId não encontrado"));

        var eventosPeriodo = eventoRepository.findEventosEntreDatas(inicio, fim);
        var eventos = eventosPeriodo.stream()
                .filter(e -> e.getProfessor() != null && Objects.equals(e.getProfessor().getId(), professor.getId()))
                .sorted(Comparator.comparing(Evento::getDataInicio))
                .toList();
        return eventos;
    }

    /**
     * Retorna a aula atual (se houver) e a próxima para um aluno, considerando matrículas ativas.
     * Resultado: lista com 0, 1 ou 2 eventos (na ordem: atual primeiro, depois próxima).
     */
    public List<Evento> aulaAtualEProximaDoAluno(Long alunoId, LocalDateTime now) {
        // valida existência do aluno
        usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("alunoId não encontrado"));

        List<Evento> atuais = eventoRepository.findEventosAtuaisDoAluno(alunoId, now);
        List<Evento> futuros = eventoRepository.findProximosEventosDoAluno(alunoId, now);

        List<Evento> result = new ArrayList<>();
        if (!atuais.isEmpty()) {
            // se houver mais de um simultâneo, pega o mais cedo
            atuais.sort(Comparator.comparing(Evento::getDataInicio));
            result.add(atuais.get(0));
            // para próxima, pega o primeiro futuro
            if (!futuros.isEmpty()) result.add(futuros.get(0));
        } else {
            // sem atual: a "atual" não existe; retorna só a próxima se houver
            if (!futuros.isEmpty()) result.add(futuros.get(0));
        }
        return result;
    }

    private void validar(CreateEventoRequest e) {
        if (!StringUtils.hasText(e.titulo)) throw new IllegalArgumentException("titulo é obrigatório");
        if (!StringUtils.hasText(e.tipoEvento)) throw new IllegalArgumentException("tipoEvento é obrigatório");
        if (e.professorId == null) throw new IllegalArgumentException("professorId é obrigatório");
        if (e.salaId == null) throw new IllegalArgumentException("salaId é obrigatório");
        if (e.inicio == null || e.fim == null) throw new IllegalArgumentException("inicio/fim são obrigatórios");
        if (!e.inicio.isBefore(e.fim)) throw new IllegalArgumentException("inicio deve ser anterior a fim");
        try { TipoEvento.valueOf(e.tipoEvento); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("tipoEvento inválido"); }
    }

    private List<SugestaoDTO> sugerir(CreateEventoRequest req, Sala salaOriginal) {
        List<SugestaoDTO> out = new ArrayList<>();

        // A) mesmo horário, outra sala disponível
        var salas = salaRepository.findAll();
        for (var s : salas) {
            if (Objects.equals(s.getId(), salaOriginal.getId())) continue;
            boolean livre = eventoRepository.findConflitosAgendamento(s, req.inicio, req.fim).isEmpty();
            if (livre) {
                out.add(new SugestaoDTO(req.inicio, req.fim, "SALA", s.getId(), "Outro recurso no mesmo horário"));
                if (out.size() >= 3) break;
            }
        }

        // B) mesmo recurso, próxima janela (+10 min)
        if (out.size() < 3) {
            out.add(new SugestaoDTO(req.inicio.plusMinutes(10), req.fim.plusMinutes(10), "SALA", salaOriginal.getId(), "Próxima janela no mesmo dia"));
        }

        // C) mesmo horário, dia seguinte
        if (out.size() < 3) {
            out.add(new SugestaoDTO(req.inicio.plusDays(1), req.fim.plusDays(1), "SALA", salaOriginal.getId(), "Mesmo horário no dia seguinte"));
        }

        return out;
    }

    public static class SchedulerConflict extends RuntimeException {
        public final String code;
        public final String publicMessage;
        public final List<SugestaoDTO> sugestoes;
        public SchedulerConflict(String code, String publicMessage, List<SugestaoDTO> sugestoes) {
            super(publicMessage);
            this.code = code;
            this.publicMessage = publicMessage;
            this.sugestoes = sugestoes;
        }
    }
}