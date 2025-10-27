package com.sistema.agendamento.sistema_agendamento.service;

import com.sistema.agendamento.sistema_agendamento.dto.SalaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.SalaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.dto.AgendaItemDTO;
import com.sistema.agendamento.sistema_agendamento.entity.*;
import com.sistema.agendamento.sistema_agendamento.exception.ConflictException;
import com.sistema.agendamento.sistema_agendamento.repository.MatriculaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.ReservaSalaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.SalaRepository;
import com.sistema.agendamento.sistema_agendamento.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaServiceTest {

    @Mock ReservaSalaRepository reservaSalaRepository;
    @Mock TurmaRepository turmaRepository;
    @Mock SalaRepository salaRepository;
    @Mock MatriculaRepository matriculaRepository;

    @InjectMocks SalaService service;

    private Sala sala;
    private Turma turma;
    private Usuario professor;

    @BeforeEach
    void setup() {
        sala = new Sala();
        // assumindo que sua entidade possui setters
        try { sala.getClass().getMethod("setId", Long.class).invoke(sala, 10L); } catch (Exception ignored) {}
        try { sala.getClass().getMethod("setCapacidade", Integer.class).invoke(sala, 40); } catch (Exception ignored) {}

        professor = new Usuario();
        try { professor.getClass().getMethod("setId", Long.class).invoke(professor, 100L); } catch (Exception ignored) {}

        turma = new Turma();
        try {
            turma.getClass().getMethod("setId", Long.class).invoke(turma, 1L);
            turma.getClass().getMethod("setProfessor", Usuario.class).invoke(turma, professor);
        } catch (Exception ignored) {}
    }

    @Test
    void alocar_deveCriarReservaQuandoDisponivel() {
        var inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var fim = inicio.plusHours(2);
        var req = new SalaRequestDTO(1L, 10L, inicio, fim, null);

        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(salaRepository.findById(10L)).thenReturn(Optional.of(sala));
        when(reservaSalaRepository.temConflito(10L, inicio, fim)).thenReturn(false);
        when(matriculaRepository.countByTurmaId(1L)).thenReturn(20L);

        var reservaSalva = new ReservaSala();
        try { reservaSalva.getClass().getMethod("setId", Long.class).invoke(reservaSalva, 999L); } catch (Exception ignored) {}
        when(reservaSalaRepository.save(any(ReservaSala.class))).thenReturn(reservaSalva);

        SalaResponseDTO resp = service.alocar(req);

        assertNotNull(resp);
        assertEquals(999L, resp.reservaId());
        assertFalse(resp.conflito());
        verify(reservaSalaRepository).save(any(ReservaSala.class));
    }

    @Test
    void alocar_deveFalharQuandoHorarioInvalido() {
        var inicio = LocalDateTime.now().plusDays(1).withHour(12);
        var fim = inicio.minusHours(1); // fim antes do inicio
        var req = new SalaRequestDTO(1L, 10L, inicio, fim, null);

        // validação de horário ocorre antes de buscar no banco
        assertThrows(IllegalArgumentException.class, () -> service.alocar(req));
        verify(reservaSalaRepository, never()).save(any());
    }

    @Test
    void alocar_deveFalharQuandoConflitoHorario() {
        var inicio = LocalDateTime.now().plusDays(1).withHour(10);
        var fim = inicio.plusHours(2);
        var req = new SalaRequestDTO(1L, 10L, inicio, fim, null);

        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(salaRepository.findById(10L)).thenReturn(Optional.of(sala));
        when(reservaSalaRepository.temConflito(10L, inicio, fim)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.alocar(req));
        verify(reservaSalaRepository, never()).save(any());
    }

    @Test
    void alocar_deveFalharQuandoCapacidadeInsuficiente() {
        var inicio = LocalDateTime.now().plusDays(1).withHour(10);
        var fim = inicio.plusHours(2);
        var req = new SalaRequestDTO(1L, 10L, inicio, fim, null);

        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(salaRepository.findById(10L)).thenReturn(Optional.of(sala));
        when(reservaSalaRepository.temConflito(10L, inicio, fim)).thenReturn(false);
        when(matriculaRepository.countByTurmaId(1L)).thenReturn(100L); // > capacidade 40

        assertThrows(ConflictException.class, () -> service.alocar(req));
        verify(reservaSalaRepository, never()).save(any());
    }

    @Test
    void agendaSalaNoMes_deveRetornarLista() {
        var periodo = YearMonth.now();
        var inicio = periodo.atDay(1).atStartOfDay();
        var fim = periodo.atEndOfMonth().atTime(23, 59, 59);

        var r = new ReservaSala();
        try {
            r.getClass().getMethod("setId", Long.class).invoke(r, 1L);
            r.getClass().getMethod("setTurma", Turma.class).invoke(r, turma);
            r.getClass().getMethod("setDataInicio", LocalDateTime.class).invoke(r, inicio.plusHours(8));
            r.getClass().getMethod("setDataFim", LocalDateTime.class).invoke(r, inicio.plusHours(10));
        } catch (Exception ignored) {}

        when(reservaSalaRepository.findaBySalaIdAndInicioBetween(10L, inicio, fim)).thenReturn(List.of(r));

        var itens = service.agendaSalaNoMes(10L, periodo);

        assertEquals(1, itens.size());
        AgendaItemDTO item = itens.get(0);
        assertEquals(1L, item.getReservaId());
        assertEquals(turma.getId(), item.getTurmaId());
    }
}