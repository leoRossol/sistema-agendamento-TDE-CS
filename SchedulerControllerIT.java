package com.sistema.agendamento.sistema_agendamento;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.agendamento.sistema_agendamento.entity.*;
import com.sistema.agendamento.sistema_agendamento.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SchedulerControllerIT {

    @LocalServerPort
    int port;

    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper om;

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired CursoRepository cursoRepository;
    @Autowired DisciplinaRepository disciplinaRepository;
    @Autowired TurmaRepository turmaRepository;
    @Autowired SalaRepository salaRepository;
    @Autowired EventoRepository eventoRepository;

    private Long professorId;
    private Long turmaId;
    private Long salaId;

    @BeforeEach
    void setup() {
        // limpa em ordem de FK
        eventoRepository.deleteAll();
        turmaRepository.deleteAll();
        disciplinaRepository.deleteAll();
        cursoRepository.deleteAll();
        salaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // professor
        Usuario prof = new Usuario();
        prof.setNome("Prof. Euler");
        prof.setEmail("euler@uni.test");
        prof.setSenha("x");
        // ajuste se seu enum tiver outro nome
        prof.setTipoUsuario(TipoUsuario.PROFESSOR);
        prof.setAtivo(true);
        professorId = usuarioRepository.save(prof).getId();

        // curso
        Curso curso = new Curso();
        curso.setNome("Engenharia");
        curso.setCodigo("ENG");
        cursoRepository.save(curso);

        // disciplina
        Disciplina d = new Disciplina();
        d.setNome("Cálculo I");
        d.setCodigo("MAT101");
        d.setCargaHoraria(60);
        d.setCurso(curso);
        d.setAtivo(true);
        disciplinaRepository.save(d);

        // turma
        Turma t = new Turma();
        t.setCodigo("TURMA_A");
        t.setAno(2025);
        t.setSemestre("2");
        t.setProfessor(prof);
        t.setDisciplina(d);
        turmaId = turmaRepository.save(t).getId();

        // sala
        Sala s = new Sala();
        s.setNome("Sala 101");
        s.setNumero("101");
        // ajuste se seu enum tiver outro nome
        s.setTipoSala(TipoSala.SALA_AULA);
        s.setCapacidade(40);
        s.setAtivo(true);
        salaId = salaRepository.save(s).getId();
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private ResponseEntity<String> postEvento(
            String tipoEvento,
            String titulo,
            LocalDateTime inicio,
            LocalDateTime fim
    ) throws Exception {
        Map<String, Object> payload = Map.of(
                "tipoEvento", tipoEvento,           // "AULA", "PROVA", ...
                "titulo", titulo,
                "descricao", "Teste",
                "professorId", professorId,
                "turmaId", turmaId,
                "salaId", salaId,
                "inicio", inicio.toString(),        // 2025-10-27T19:00:00
                "fim", fim.toString()
        );

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(om.writeValueAsString(payload), h);
        return rest.postForEntity(baseUrl() + "/scheduler/eventos", req, String.class);
    }

    @Test
    void criaEvento_semConflito_retorna201_eStatusConfirmado() throws Exception {
        LocalDateTime ini = LocalDateTime.of(2025, 10, 27, 19, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 10, 27, 21, 0, 0);

        ResponseEntity<String> resp = postEvento("AULA", "Cálculo I", ini, fim);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode body = om.readTree(resp.getBody());
        assertThat(body.path("id").asLong()).isPositive();
        // sua API pode usar CONFIRMADO ou AGENDADO — ajuste se necessário
        assertThat(body.path("status").asText()).isIn("CONFIRMADO", "AGENDADO");
    }

    @Test
    void criarEvento_comConflitoNaMesmaSala_retorna409() throws Exception {
        // cria primeiro
        LocalDateTime ini1 = LocalDateTime.of(2025, 10, 27, 19, 0, 0);
        LocalDateTime fim1 = LocalDateTime.of(2025, 10, 27, 21, 0, 0);
        ResponseEntity<String> ok = postEvento("AULA", "Cálculo I", ini1, fim1);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // tenta conflitar
        LocalDateTime ini2 = LocalDateTime.of(2025, 10, 27, 20, 0, 0);
        LocalDateTime fim2 = LocalDateTime.of(2025, 10, 27, 22, 0, 0);
        ResponseEntity<String> resp = postEvento("AULA", "Cálculo II", ini2, fim2);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        if (resp.getBody() != null && !resp.getBody().isBlank()) {
            JsonNode body = om.readTree(resp.getBody());
            // muitas APIs retornam {mensagem, sugestoes:[...]} — validamos de forma tolerante
            assertThat(body.path("mensagem").asText()).isNotEmpty();
            if (body.has("sugestoes")) {
                assertThat(body.path("sugestoes").isArray()).isTrue();
            }
        }
    }

    @Test
    void getEvento_porId_200() throws Exception {
        LocalDateTime ini = LocalDateTime.of(2025, 10, 27, 19, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 10, 27, 21, 0, 0);

        ResponseEntity<String> created = postEvento("AULA", "Cálculo I", ini, fim);
        Long id = om.readTree(created.getBody()).path("id").asLong();

        ResponseEntity<String> get = rest.getForEntity(baseUrl() + "/scheduler/eventos/" + id, String.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode body = om.readTree(get.getBody());
        assertThat(body.path("id").asLong()).isEqualTo(id);
        assertThat(body.path("titulo").asText()).isEqualTo("Cálculo I");
    }

    @Test
    void calendarioProfessor_porPeriodo_200_eTemItens() throws Exception {
        // garante 1 evento no período
        LocalDateTime ini = LocalDateTime.of(2025, 10, 27, 19, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 10, 27, 21, 0, 0);
        ResponseEntity<String> created = postEvento("AULA", "Cálculo I", ini, fim);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String periodo = "2025-10-20T00:00:00/2025-10-31T23:59:59";
        String url = baseUrl() + "/scheduler/calendario/professores/" + professorId + "?periodo=" + periodo;

        ResponseEntity<String> resp = rest.getForEntity(url, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode arr = om.readTree(resp.getBody());
        assertThat(arr.isArray()).isTrue();
        assertThat(arr.size()).isGreaterThanOrEqualTo(1);
    }
}