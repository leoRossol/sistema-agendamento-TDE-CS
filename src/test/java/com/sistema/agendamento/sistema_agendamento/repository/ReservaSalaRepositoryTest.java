//package com.sistema.agendamento.sistema_agendamento.repository;
//
//import com.sistema.agendamento.sistema_agendamento.entity.ReservaSala;
//import com.sistema.agendamento.sistema_agendamento.entity.Sala;
//import com.sistema.agendamento.sistema_agendamento.entity.Turma;
//import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
//import com.sistema.agendamento.sistema_agendamento.enums.TipoSala;
//import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//
//import java.lang.reflect.Field;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//class ReservaSalaRepositoryTest {
//
//    @Autowired ReservaSalaRepository reservaRepo;
//    @Autowired SalaRepository salaRepo;
//    @Autowired TurmaRepository turmaRepo;
//    @Autowired UsuarioRepository usuarioRepo;
//    @Autowired TestEntityManager em;
//
//    @Test
//    void temConflito_deveDetectarSobreposicao() {
//        Sala sala = salaRepo.save(novaSala("Lab 101", "101", 30, TipoSala.SALA_AULA));
//        Usuario prof = usuarioRepo.save(novoProfessor("prof@teste.com", "Prof A"));
//        Turma turma = turmaRepo.save(novaTurma("T1", "2025-2", 2025, prof));
//
//        // existente 10-12
//        var r1 = new ReservaSala();
//        r1.setSala(sala);
//        r1.setProfessor(prof);
//        r1.setTurma(turma);
//        r1.setDataInicio(LocalDateTime.of(2025,10,20,10,0));
//        r1.setDataFim(LocalDateTime.of(2025,10,20,12,0));
//        reservaRepo.save(r1);
//        em.flush(); // garante visibilidade na consulta
//
//        // conflito 11-13
//        boolean conflito = reservaRepo.temConflito(
//            sala.getId(),
//            LocalDateTime.of(2025,10,20,11,0),
//            LocalDateTime.of(2025,10,20,13,0)
//        );
//        assertThat(conflito).isTrue();
//
//        // sem conflito 12-14 (toca na borda)
//        boolean semConflito = reservaRepo.temConflito(
//            sala.getId(),
//            LocalDateTime.of(2025,10,20,12,0),
//            LocalDateTime.of(2025,10,20,14,0)
//        );
//        assertThat(semConflito).isFalse();
//    }
//
//    @Test
//    void temConflito_intervaloContido_totalmenteDentro_conflito() {
//        Sala sala = salaRepo.save(novaSala("Sala A", "S-A", 40, TipoSala.SALA_AULA));
//        Usuario prof = usuarioRepo.save(novoProfessor("prof2@teste.com", "Prof B"));
//        Turma turma = turmaRepo.save(novaTurma("T2", "2025-2", 2025, prof));
//
//        var r1 = new ReservaSala();
//        r1.setSala(sala);
//        r1.setProfessor(prof);
//        r1.setTurma(turma);
//        r1.setDataInicio(LocalDateTime.of(2025,10,21,10,0));
//        r1.setDataFim(LocalDateTime.of(2025,10,21,12,0));
//        reservaRepo.save(r1);
//        em.flush();
//
//        boolean conflito = reservaRepo.temConflito(
//            sala.getId(),
//            LocalDateTime.of(2025,10,21,10,30),
//            LocalDateTime.of(2025,10,21,11,30)
//        );
//        assertThat(conflito).isTrue();
//    }
//
//    @Test
//    void temConflito_intervaloEnglobaReserva_conflito() {
//        Sala sala = salaRepo.save(novaSala("Sala B", "S-B", 35, TipoSala.SALA_AULA));
//        Usuario prof = usuarioRepo.save(novoProfessor("prof3@teste.com", "Prof C"));
//        Turma turma = turmaRepo.save(novaTurma("T3", "2025-2", 2025, prof));
//
//        var r1 = new ReservaSala();
//        r1.setSala(sala);
//        r1.setProfessor(prof);
//        r1.setTurma(turma);
//        r1.setDataInicio(LocalDateTime.of(2025,10,22,10,0));
//        r1.setDataFim(LocalDateTime.of(2025,10,22,12,0));
//        reservaRepo.save(r1);
//        em.flush();
//
//        boolean conflito = reservaRepo.temConflito(
//            sala.getId(),
//            LocalDateTime.of(2025,10,22,9,0),
//            LocalDateTime.of(2025,10,22,13,0)
//        );
//        assertThat(conflito).isTrue();
//    }
//
//    @Test
//    void temConflito_bordasSemConflito_antesEDepois() {
//        Sala sala = salaRepo.save(novaSala("Sala C", "S-C", 25, TipoSala.SALA_AULA));
//        Usuario prof = usuarioRepo.save(novoProfessor("prof4@teste.com", "Prof D"));
//        Turma turma = turmaRepo.save(novaTurma("T4", "2025-2", 2025, prof));
//
//        var r1 = new ReservaSala();
//        r1.setSala(sala);
//        r1.setProfessor(prof);
//        r1.setTurma(turma);
//        r1.setDataInicio(LocalDateTime.of(2025,10,23,10,0));
//        r1.setDataFim(LocalDateTime.of(2025,10,23,12,0));
//        reservaRepo.save(r1);
//        em.flush();
//
//        boolean semConflitoAntes = reservaRepo.temConflito(
//            sala.getId(),
//            LocalDateTime.of(2025,10,23,8,0),
//            LocalDateTime.of(2025,10,23,10,0)
//        );
//        assertThat(semConflitoAntes).isFalse();
//
//        boolean semConflitoDepois = reservaRepo.temConflito(
//            sala.getId(),
//            LocalDateTime.of(2025,10,23,12,0),
//            LocalDateTime.of(2025,10,23,14,0)
//        );
//        assertThat(semConflitoDepois).isFalse();
//    }
//
//    @Test
//    void temConflito_outraSala_semConflito() {
//        Sala sala1 = salaRepo.save(novaSala("Sala 1", "S1", 40, TipoSala.SALA_AULA));
//        Sala sala2 = salaRepo.save(novaSala("Sala 2", "S2", 40, TipoSala.SALA_AULA));
//        Usuario prof = usuarioRepo.save(novoProfessor("prof5@teste.com", "Prof E"));
//        Turma turma = turmaRepo.save(novaTurma("T5", "2025-2", 2025, prof));
//
//        var r1 = new ReservaSala();
//        r1.setSala(sala1);
//        r1.setProfessor(prof);
//        r1.setTurma(turma);
//        r1.setDataInicio(LocalDateTime.of(2025,10,24,10,0));
//        r1.setDataFim(LocalDateTime.of(2025,10,24,12,0));
//        reservaRepo.save(r1);
//        em.flush();
//
//        boolean semConflito = reservaRepo.temConflito(
//            sala2.getId(),
//            LocalDateTime.of(2025,10,24,11,0),
//            LocalDateTime.of(2025,10,24,13,0)
//        );
//        assertThat(semConflito).isFalse();
//    }
//
//    @Test
//    void daSalaNoPeriodo_deveRetornarReservasDoMesOrdenadasEInclusivas() {
//        Sala sala = salaRepo.save(novaSala("Sala 1", "S1", 40, TipoSala.SALA_AULA));
//        Usuario prof = usuarioRepo.save(novoProfessor("prof2@teste.com", "Prof B"));
//        Turma turma = turmaRepo.save(novaTurma("T2", "2025-2", 2025, prof));
//
//        // 2025-10-01 00:00 (limite inferior, deve entrar)
//        var r0 = new ReservaSala();
//        r0.setSala(sala); r0.setProfessor(prof); r0.setTurma(turma);
//        r0.setDataInicio(LocalDateTime.of(2025, 10, 1, 0, 0));
//        r0.setDataFim(LocalDateTime.of(2025, 10, 1, 2, 0));
//        reservaRepo.save(r0);
//
//        // meio
//        var r1 = new ReservaSala();
//        r1.setSala(sala); r1.setProfessor(prof); r1.setTurma(turma);
//        r1.setDataInicio(LocalDateTime.of(2025, 10, 10, 9, 0));
//        r1.setDataFim(LocalDateTime.of(2025, 10, 10, 11, 0));
//        reservaRepo.save(r1);
//
//        // 2025-10-31 23:59:59 (limite superior, deve entrar)
//        var r2 = new ReservaSala();
//        r2.setSala(sala); r2.setProfessor(prof); r2.setTurma(turma);
//        r2.setDataInicio(LocalDateTime.of(2025, 10, 31, 23, 59, 59));
//        r2.setDataFim(LocalDateTime.of(2025, 11, 1, 1, 0));
//        reservaRepo.save(r2);
//        em.flush();
//
//        var inicio = LocalDateTime.of(2025,10,1,0,0);
//        var fim = LocalDateTime.of(2025,10,31,23,59,59);
//
//        List<ReservaSala> lista = reservaRepo.findaBySalaIdAndInicioBetween(sala.getId(), inicio, fim);
//
//        assertThat(lista).hasSize(3);
//        assertThat(lista.get(0).getDataInicio()).isEqualTo(r0.getDataInicio());
//        assertThat(lista.get(1).getDataInicio()).isEqualTo(r1.getDataInicio());
//        assertThat(lista.get(2).getDataInicio()).isEqualTo(r2.getDataInicio());
//    }
//
//    // ========= helpers =========
//
//    private Sala novaSala(String nome, String numero, int capacidade, TipoSala tipo) {
//        Sala s = new Sala();
//        set(s, "nome", nome);
//        set(s, "numero", numero);
//        set(s, "capacidade", capacidade);
//        set(s, "tipoSala", tipo);
//        return s;
//    }
//
//    private Usuario novoProfessor(String email, String nome) {
//        Usuario u = new Usuario();
//        set(u, "email", email);
//        set(u, "nome", nome);
//        set(u, "senha", "123456");
//        set(u, "tipoUsuario", TipoUsuario.PROFESSOR);
//        return u;
//    }
//
//    private Turma novaTurma(String codigo, String semestre, int ano, Usuario professor) {
//        Turma t = new Turma();
//        set(t, "codigo", codigo);
//        set(t, "semestre", semestre);
//        set(t, "ano", ano);
//        set(t, "professor", professor);
//        return t;
//    }
//
//    private static void set(Object target, String fieldName, Object value) {
//        try {
//            Field f = target.getClass().getDeclaredField(fieldName);
//            f.setAccessible(true);
//            f.set(target, value);
//        } catch (Exception e) {
//            throw new RuntimeException("Falha ao setar campo " + fieldName + " em " + target.getClass(), e);
//        }
//    }
//}