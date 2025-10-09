package com.sistema.agendamento.sistema_agendamento.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sistema.agendamento.sistema_agendamento.dto.CreateEventoRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.UpdateEventoRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.SugestaoDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Evento;
import com.sistema.agendamento.sistema_agendamento.entity.Notificacao;
import com.sistema.agendamento.sistema_agendamento.entity.Sala;
import com.sistema.agendamento.sistema_agendamento.entity.Turma;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.entity.WaitlistEntry;
import com.sistema.agendamento.sistema_agendamento.enums.StatusEventos;
import com.sistema.agendamento.sistema_agendamento.enums.TipoEvento;
import com.sistema.agendamento.sistema_agendamento.repository.EventoRepository;
import com.sistema.agendamento.sistema_agendamento.repository.NotificacaoRepository;
import com.sistema.agendamento.sistema_agendamento.repository.SalaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.TurmaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import com.sistema.agendamento.sistema_agendamento.repository.WaitlistRepository;

@Service
public class SchedulerService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final SalaRepository salaRepository;
    private final WaitlistRepository waitlistRepository;
    private final NotificacaoRepository notificacaoRepository;

    public SchedulerService(EventoRepository eventoRepository,
                            UsuarioRepository usuarioRepository,
                            TurmaRepository turmaRepository,
                            SalaRepository salaRepository,
                            WaitlistRepository waitlistRepository,
                            NotificacaoRepository notificacaoRepository) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.salaRepository = salaRepository;
        this.waitlistRepository = waitlistRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @org.springframework.transaction.annotation.Transactional
    public Evento criarEvento(CreateEventoRequestDTO req) {
        validar(req);

        Usuario professor = usuarioRepository.findById(req.professorId)
                .orElseThrow(() -> new IllegalArgumentException("professorId não encontrado"));

        Turma turma = null;
        if (req.turmaId != null) {
            turma = turmaRepository.findById(req.turmaId)
                    .orElseThrow(() -> new IllegalArgumentException("turmaId não encontrado"));
        }

        // US-09: múltiplos labs em conjunto
        List<Sala> salasSelecionadas = new ArrayList<>();
        if (req.labs != null && !req.labs.isEmpty()) {
            // precisa de pelo menos 2 labs
            if (req.labs.size() < 2) {
                throw new IllegalArgumentException("Para reserva conjunta informe ao menos 2 labs em 'labs'");
            }
            // carrega todas e valida existência
            for (Long sid : req.labs) {
                Sala s = salaRepository.findById(sid)
                        .orElseThrow(() -> new IllegalArgumentException("labId=" + sid + " não encontrado"));
                salasSelecionadas.add(s);
            }
            // valida vínculo de conjunto: todas marcadas como ehConjunto e pertencentes ao grupo do primeiro
            Sala principal = salasSelecionadas.get(0);
            if (principal.getEhConjunto() == null || !principal.getEhConjunto()) {
                throw new IllegalArgumentException("labs não pertencem a um conjunto válido");
            }
            // checa que cada sala está marcada como conjunto e (se modelado) aparece nas ligações do principal
            for (int i = 1; i < salasSelecionadas.size(); i++) {
                Sala s = salasSelecionadas.get(i);
                if (s.getEhConjunto() == null || !s.getEhConjunto()) {
                    throw new IllegalArgumentException("labs não pertencem a um conjunto válido");
                }
                // se houver relação explicitada, exige presença
                if (principal.getSalasConjuntas() != null && !principal.getSalasConjuntas().isEmpty()) {
                    if (!principal.getSalasConjuntas().stream().anyMatch(x -> x.getId().equals(s.getId()))) {
                        throw new IllegalArgumentException("labs não vinculados no mesmo conjunto");
                    }
                }
            }
        }

        Sala sala = null;
        if (salasSelecionadas.isEmpty()) {
            sala = salaRepository.findById(req.salaId)
                    .orElseThrow(() -> new IllegalArgumentException("salaId não encontrado"));
            salasSelecionadas.add(sala);
        }

        boolean conflitoSala;
        if (salasSelecionadas.size() > 1) {
            conflitoSala = !eventoRepository.findConflitosAgendamentoSalas(salasSelecionadas, req.inicio, req.fim).isEmpty();
        } else {
            conflitoSala = !eventoRepository.findConflitosAgendamento(salasSelecionadas.get(0), req.inicio, req.fim).isEmpty();
        }
        boolean conflitoProfessor = !eventoRepository.findConflitosProfessor(professor, req.inicio, req.fim).isEmpty();
        boolean conflitoTurma = (turma != null) && !eventoRepository.findConflitosTurma(turma, req.inicio, req.fim).isEmpty();

        if (conflitoSala || conflitoProfessor || conflitoTurma) {
            // usa a primeira sala como referência de sugestão quando multi-labs
            Sala refSala = salasSelecionadas.isEmpty() ? null : salasSelecionadas.get(0);
            List<SugestaoDTO> sug = sugerir(req, refSala);
            throw new SchedulerConflict("CONFLITO_AGENDA", "Conflito detectado com recurso/professor/turma.", sug);
        }

        // Criação atômica: cria um evento por sala selecionada; se falhar, transação reverte tudo
        Evento primeiro = null;
        for (Sala s : salasSelecionadas) {
            Evento e = new Evento();
            e.setTitulo(req.titulo);
            e.setDescricao(req.descricao);
            e.setDataInicio(req.inicio);
            e.setDataFim(req.fim);
            e.setTipoEvento(TipoEvento.valueOf(req.tipoEvento));
            e.setProfessor(professor);
            e.setTurma(turma);
            e.setSala(s);
            e.setStatus(StatusEventos.CONFIRMADO);
            Evento saved = eventoRepository.save(e);
            if (primeiro == null) primeiro = saved;
        }
        return primeiro;
    }

    public Evento obterEvento(Long id) {
        return eventoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Evento não encontrado"));
    }

    public Evento atualizarEvento(Long id, CreateEventoRequestDTO req) {
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

    /**
     * PATCH: gerenciamento por professor (edição simples/cancelamento) — valida owner.
     */
    @org.springframework.transaction.annotation.Transactional
    public Evento patchEvento(Long id, UpdateEventoRequestDTO req) {
        Evento existente = eventoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evento não encontrado"));

        if (req.ownerId == null)
            throw new IllegalArgumentException("ownerId é obrigatório");
        if (existente.getProfessor() == null || !Objects.equals(existente.getProfessor().getId(), req.ownerId))
            throw new IllegalArgumentException("Somente o professor dono do evento pode gerenciar");

        // Cancelamento
        if ("CANCELADO".equalsIgnoreCase(req.status)) {
            if (existente.getStatus() == StatusEventos.CANCELADO) {
                return existente; // idempotente
            }
            if (existente.getStatus() == null || existente.getStatus() == StatusEventos.CONFIRMADO || existente.getStatus() == StatusEventos.AGENDADO) {
                existente.setStatus(StatusEventos.CANCELADO);
                Evento salvo = eventoRepository.save(existente);
                // notificar inscritos (alunos) e waitlist de liberação
                notificarCancelamentoParaInscritos(salvo);
                notificarWaitlistLiberacao(salvo);
                return salvo;
            } else {
                throw new IllegalArgumentException("Status atual não permite cancelamento");
            }
        }

        // Edicao leve (opcional, sem checar conflito aqui para escopo reduzido)
        if (req.titulo != null) existente.setTitulo(req.titulo);
        if (req.descricao != null) existente.setDescricao(req.descricao);
        if (req.inicio != null) existente.setDataInicio(req.inicio);
        if (req.fim != null) existente.setDataFim(req.fim);
        if (req.salaId != null) {
            Sala sala = salaRepository.findById(req.salaId)
                    .orElseThrow(() -> new IllegalArgumentException("salaId não encontrado"));
            existente.setSala(sala);
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

    /**
     * US-14: obter todas as aulas do dia informado.
     */
    public List<Evento> aulasDoDia(LocalDate data) {
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = inicio.plusDays(1);
        return eventoRepository.findEventosDoPeriodo(inicio, fim)
                .stream()
                .sorted(Comparator.comparing(Evento::getDataInicio))
                .toList();
    }

    private void validar(CreateEventoRequestDTO e) {
        if (!StringUtils.hasText(e.titulo)) throw new IllegalArgumentException("titulo é obrigatório");
        if (!StringUtils.hasText(e.tipoEvento)) throw new IllegalArgumentException("tipoEvento é obrigatório");
        if (e.professorId == null) throw new IllegalArgumentException("professorId é obrigatório");
        if ((e.labs == null || e.labs.isEmpty()) && e.salaId == null)
            throw new IllegalArgumentException("salaId é obrigatório quando labs não informado");
        if (e.inicio == null || e.fim == null) throw new IllegalArgumentException("inicio/fim são obrigatórios");
        if (!e.inicio.isBefore(e.fim)) throw new IllegalArgumentException("inicio deve ser anterior a fim");
        try { TipoEvento.valueOf(e.tipoEvento); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("tipoEvento inválido"); }
    }

    private List<SugestaoDTO> sugerir(CreateEventoRequestDTO req, Sala salaOriginal) {
        List<SugestaoDTO> out = new ArrayList<>();

        // A) mesmo horário, outra sala disponível
        var salas = salaRepository.findAll();
        for (var s : salas) {
            if (salaOriginal != null && Objects.equals(s.getId(), salaOriginal.getId())) continue;
            boolean livre = eventoRepository.findConflitosAgendamento(s, req.inicio, req.fim).isEmpty();
            if (livre) {
                out.add(new SugestaoDTO(req.inicio, req.fim, "SALA", s.getId(), "Outro recurso no mesmo horário"));
                if (out.size() >= 3) break;
            }
        }

        // B) mesmo recurso, próxima janela (+10 min)
        if (out.size() < 3 && salaOriginal != null) {
            out.add(new SugestaoDTO(req.inicio.plusMinutes(10), req.fim.plusMinutes(10), "SALA", salaOriginal.getId(), "Próxima janela no mesmo dia"));
        }

        // C) mesmo horário, dia seguinte
        if (out.size() < 3 && salaOriginal != null) {
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

    // ===================== WAITLIST (US-11) =====================

    @org.springframework.transaction.annotation.Transactional
    public WaitlistResult entrarNaWaitlist(Long labId, Long professorId, LocalDateTime inicio, LocalDateTime fim) {
        if (labId == null) throw new IllegalArgumentException("labId é obrigatório");
        if (professorId == null) throw new IllegalArgumentException("professorId é obrigatório");
        if (inicio == null || fim == null || !inicio.isBefore(fim))
            throw new IllegalArgumentException("janela inválida");

        Sala sala = salaRepository.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("labId não encontrado"));
        Usuario professor = usuarioRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("professorId não encontrado"));

        // posição = quantos já estão na fila (WAITING/NOTIFIED não expirado) antes deste
        var considerados = java.util.List.of(WaitlistEntry.Status.WAITING, WaitlistEntry.Status.NOTIFIED);
        int position = (int) waitlistRepository.countBySalaAndStatusIn(sala, considerados) + 1;

        WaitlistEntry entry = new WaitlistEntry();
        entry.setSala(sala);
        entry.setProfessor(professor);
        entry.setJanelaInicio(inicio);
        entry.setJanelaFim(fim);
        entry.setStatus(WaitlistEntry.Status.WAITING);
        WaitlistEntry saved = waitlistRepository.save(entry);

        return new WaitlistResult(saved.getId(), position);
    }

    @org.springframework.transaction.annotation.Transactional
    public boolean claimWaitlist(Long entryId, Long professorId) {
        WaitlistEntry entry = waitlistRepository.findById(entryId)
                .orElseThrow(() -> new NoSuchElementException("Entrada de waitlist não encontrada"));
        if (professorId != null && (entry.getProfessor() == null || !Objects.equals(entry.getProfessor().getId(), professorId))) {
            throw new IllegalArgumentException("Somente o professor da entrada pode dar claim");
        }
        // Verifica expiração se foi notificado
        if (entry.getStatus() == WaitlistEntry.Status.NOTIFIED) {
            if (entry.getNotifyExpiresAt() != null && LocalDateTime.now().isAfter(entry.getNotifyExpiresAt())) {
                entry.setStatus(WaitlistEntry.Status.EXPIRED);
                waitlistRepository.save(entry);
                return false;
            }
        }

        // Somente permite claim quando NOTIFIED ou WAITING e houver disponibilidade
        // Disponibilidade: sem conflitos naquele intervalo e sala
        boolean conflitoSala = !eventoRepository.findConflitosAgendamento(entry.getSala(), entry.getJanelaInicio(), entry.getJanelaFim()).isEmpty();
        if (conflitoSala) {
            return false;
        }

        // cria reserva simples (Evento) para o professor
        Evento e = new Evento();
        e.setTitulo("Reserva por espera - Sala " + entry.getSala().getNome());
        e.setDescricao("Reserva realizada via lista de espera");
        e.setDataInicio(entry.getJanelaInicio());
        e.setDataFim(entry.getJanelaFim());
        e.setTipoEvento(TipoEvento.OUTROS);
        e.setProfessor(entry.getProfessor());
        e.setTurma(null);
        e.setSala(entry.getSala());
        e.setStatus(StatusEventos.AGENDADO);
        eventoRepository.save(e);

        entry.setStatus(WaitlistEntry.Status.CLAIMED);
        waitlistRepository.save(entry);
        return true;
    }

    private void notificarWaitlistLiberacao(Evento cancelado) {
        if (cancelado.getSala() == null) return;
        var candidatos = waitlistRepository.findWaitingOverlapping(cancelado.getSala(), cancelado.getDataInicio(), cancelado.getDataFim());
        if (candidatos.isEmpty()) return;
        WaitlistEntry primeiro = candidatos.get(0);
        // Notifica o primeiro da fila
        Notificacao n = new Notificacao();
        n.setUsuario(primeiro.getProfessor());
        n.setTitulo("Sala liberada: " + cancelado.getSala().getNome());
        n.setMensagem("Sua janela pediu: " + primeiro.getJanelaInicio() + " - " + primeiro.getJanelaFim() + ". Você tem 2h para confirmar em /scheduler/waitlist/" + primeiro.getId() + "/claim");
        n.setTipo(Notificacao.TipoNotificacao.SALA_DISPONIVEL);
        n.setEvento(cancelado);
        notificacaoRepository.save(n);

        primeiro.setStatus(WaitlistEntry.Status.NOTIFIED);
        primeiro.setNotifyExpiresAt(LocalDateTime.now().plusHours(2));
        waitlistRepository.save(primeiro);
    }

    private void notificarCancelamentoParaInscritos(Evento evento) {
        Turma turma = evento.getTurma();
        if (turma == null || turma.getAlunos() == null) return;
        for (Usuario aluno : turma.getAlunos()) {
            Notificacao n = new Notificacao();
            n.setUsuario(aluno);
            n.setTitulo("Aula cancelada: " + evento.getTitulo());
            n.setMensagem("O evento foi cancelado. Consulte a agenda para mais detalhes.");
            n.setTipo(Notificacao.TipoNotificacao.GERAL);
            n.setEvento(evento);
            notificacaoRepository.save(n);
        }
    }

    public record WaitlistResult(Long id, int position) {}
}