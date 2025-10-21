package com.sistema.agendamento.sistema_agendamento.service;

import com.sistema.agendamento.sistema_agendamento.dto.RelatorioOcupacaoRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.RelatorioOcupacaoResponseDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Evento;
import com.sistema.agendamento.sistema_agendamento.entity.Sala;
import com.sistema.agendamento.sistema_agendamento.repository.ReportsRepository;
import com.sistema.agendamento.sistema_agendamento.repository.SalaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportsService {

    private final ReportsRepository reportsRepository;
    private final SalaRepository salaRepository;

    @Transactional(readOnly = true)
    public RelatorioOcupacaoResponseDTO gerarRelatorioOcupacao(RelatorioOcupacaoRequestDTO request) {
        log.info("Gerando relatório de ocupação para período: {}", request.getPeriodo());

        String[] partes = request.getPeriodo().split("/");
        Integer ano = Integer.parseInt(partes[0]);
        String semestre = partes[1];

        // Definir período acadêmico (aproximadamente 4 meses por semestre)
        LocalDateTime[] periodoAcademico = calcularPeriodoAcademico(ano, semestre);

        // Buscar eventos do período com filtros
        List<Evento> eventos = buscarEventosComFiltros(semestre, ano, request.getCursoId(), 
                                                      request.getDisciplinaId(), request.getSalaId());

        // Gerar relatórios
        RelatorioOcupacaoResponseDTO response = new RelatorioOcupacaoResponseDTO();
        response.setPeriodo(request.getPeriodo());
        response.setOcupacaoPorSala(calcularOcupacaoPorSala(eventos, periodoAcademico));
        response.setOcupacaoPorCurso(calcularOcupacaoPorCurso(eventos, periodoAcademico));
        response.setOcupacaoPorDisciplina(calcularOcupacaoPorDisciplina(eventos, periodoAcademico));
        response.setResumo(calcularResumoOcupacao(eventos, periodoAcademico));

        log.info("Relatório gerado com sucesso para período: {}", request.getPeriodo());
        return response;
    }

    private List<Evento> buscarEventosComFiltros(String semestre, Integer ano, Long cursoId, Long disciplinaId, Long salaId) {
        if (salaId != null) {
            return reportsRepository.findEventosPorPeriodoESala(semestre, ano, salaId);
        } else if (disciplinaId != null) {
            return reportsRepository.findEventosPorPeriodoEDisciplina(semestre, ano, disciplinaId);
        } else if (cursoId != null) {
            return reportsRepository.findEventosPorPeriodoECurso(semestre, ano, cursoId);
        } else {
            return reportsRepository.findEventosPorPeriodo(semestre, ano);
        }
    }

    private LocalDateTime[] calcularPeriodoAcademico(Integer ano, String semestre) {
        LocalDate inicio;
        LocalDate fim;

        if ("1".equals(semestre)) {
            // Primeiro semestre: fevereiro a junho
            inicio = LocalDate.of(ano, 2, 1);
            fim = LocalDate.of(ano, 6, 30);
        } else {
            // Segundo semestre: agosto a dezembro
            inicio = LocalDate.of(ano, 8, 1);
            fim = LocalDate.of(ano, 12, 31);
        }

        return new LocalDateTime[]{
            inicio.atStartOfDay(),
            fim.atTime(23, 59, 59)
        };
    }

    private List<RelatorioOcupacaoResponseDTO.OcupacaoPorSalaDTO> calcularOcupacaoPorSala(
            List<Evento> eventos, LocalDateTime[] periodoAcademico) {

        Map<Long, List<Evento>> eventosPorSala = eventos.stream()
            .collect(Collectors.groupingBy(e -> e.getSala().getId()));

        List<RelatorioOcupacaoResponseDTO.OcupacaoPorSalaDTO> ocupacaoPorSala = new ArrayList<>();

        for (Map.Entry<Long, List<Evento>> entry : eventosPorSala.entrySet()) {
            Long salaId = entry.getKey();
            List<Evento> eventosSala = entry.getValue();

            if (!eventosSala.isEmpty()) {
                Sala sala = eventosSala.get(0).getSala();
                
                // Calcular horas utilizadas
                int totalHorasUtilizadas = eventosSala.stream()
                    .mapToInt(this::calcularDuracaoEmHoras)
                    .sum();

                // Calcular horas disponíveis (16 horas por dia, 7 dias por semana)
                long diasPeriodo = ChronoUnit.DAYS.between(
                    periodoAcademico[0].toLocalDate(),
                    periodoAcademico[1].toLocalDate()) + 1;
                int totalHorasDisponiveis = (int) (diasPeriodo * 16); // 16 horas por dia útil

                BigDecimal taxaOcupacao = calcularTaxaOcupacao(totalHorasUtilizadas, totalHorasDisponiveis);

                RelatorioOcupacaoResponseDTO.OcupacaoPorSalaDTO ocupacao = 
                    new RelatorioOcupacaoResponseDTO.OcupacaoPorSalaDTO(
                        sala.getId(),
                        sala.getNome(),
                        sala.getNumero(),
                        sala.getCapacidade(),
                        totalHorasUtilizadas,
                        totalHorasDisponiveis,
                        taxaOcupacao,
                        gerarDetalhesOcupacaoPorSala(salaId, eventosSala)
                    );

                ocupacaoPorSala.add(ocupacao);
            }
        }

        return ocupacaoPorSala;
    }

    private List<RelatorioOcupacaoResponseDTO.OcupacaoPorCursoDTO> calcularOcupacaoPorCurso(
            List<Evento> eventos, LocalDateTime[] periodoAcademico) {

        Map<Long, List<Evento>> eventosPorCurso = eventos.stream()
            .filter(e -> e.getTurma() != null && 
                        e.getTurma().getDisciplina() != null && 
                        e.getTurma().getDisciplina().getCurso() != null)
            .collect(Collectors.groupingBy(e -> e.getTurma().getDisciplina().getCurso().getId()));

        List<RelatorioOcupacaoResponseDTO.OcupacaoPorCursoDTO> ocupacaoPorCurso = new ArrayList<>();

        for (Map.Entry<Long, List<Evento>> entry : eventosPorCurso.entrySet()) {
            List<Evento> eventosCurso = entry.getValue();

            if (!eventosCurso.isEmpty()) {
                var curso = eventosCurso.get(0).getTurma().getDisciplina().getCurso();

                int totalHorasUtilizadas = eventosCurso.stream()
                    .mapToInt(this::calcularDuracaoEmHoras)
                    .sum();

                Set<Long> salasUtilizadas = eventosCurso.stream()
                    .map(e -> e.getSala().getId())
                    .collect(Collectors.toSet());

                long diasPeriodo = ChronoUnit.DAYS.between(
                    periodoAcademico[0].toLocalDate(),
                    periodoAcademico[1].toLocalDate()) + 1;
                int totalHorasDisponiveis = (int) (diasPeriodo * salasUtilizadas.size() * 16);

                BigDecimal taxaOcupacao = calcularTaxaOcupacao(totalHorasUtilizadas, totalHorasDisponiveis);

                RelatorioOcupacaoResponseDTO.OcupacaoPorCursoDTO ocupacao = 
                    new RelatorioOcupacaoResponseDTO.OcupacaoPorCursoDTO(
                        curso.getId(),
                        curso.getNome(),
                        curso.getCodigo(),
                        totalHorasUtilizadas,
                        totalHorasDisponiveis,
                        taxaOcupacao,
                        salasUtilizadas.size()
                    );

                ocupacaoPorCurso.add(ocupacao);
            }
        }

        return ocupacaoPorCurso;
    }

    private List<RelatorioOcupacaoResponseDTO.OcupacaoPorDisciplinaDTO> calcularOcupacaoPorDisciplina(
            List<Evento> eventos, LocalDateTime[] periodoAcademico) {

        Map<Long, List<Evento>> eventosPorDisciplina = eventos.stream()
            .filter(e -> e.getTurma() != null && e.getTurma().getDisciplina() != null)
            .collect(Collectors.groupingBy(e -> e.getTurma().getDisciplina().getId()));

        List<RelatorioOcupacaoResponseDTO.OcupacaoPorDisciplinaDTO> ocupacaoPorDisciplina = new ArrayList<>();

        for (Map.Entry<Long, List<Evento>> entry : eventosPorDisciplina.entrySet()) {
            List<Evento> eventosDisciplina = entry.getValue();

            if (!eventosDisciplina.isEmpty()) {
                var disciplina = eventosDisciplina.get(0).getTurma().getDisciplina();
                var curso = disciplina.getCurso();

                int totalHorasUtilizadas = eventosDisciplina.stream()
                    .mapToInt(this::calcularDuracaoEmHoras)
                    .sum();

                Set<Long> salasUtilizadas = eventosDisciplina.stream()
                    .map(e -> e.getSala().getId())
                    .collect(Collectors.toSet());

                Set<Long> turmasUtilizadas = eventosDisciplina.stream()
                    .map(e -> e.getTurma().getId())
                    .collect(Collectors.toSet());

                long diasPeriodo = ChronoUnit.DAYS.between(
                    periodoAcademico[0].toLocalDate(),
                    periodoAcademico[1].toLocalDate()) + 1;
                int totalHorasDisponiveis = (int) (diasPeriodo * salasUtilizadas.size() * 16);

                BigDecimal taxaOcupacao = calcularTaxaOcupacao(totalHorasUtilizadas, totalHorasDisponiveis);

                RelatorioOcupacaoResponseDTO.OcupacaoPorDisciplinaDTO ocupacao = 
                    new RelatorioOcupacaoResponseDTO.OcupacaoPorDisciplinaDTO(
                        disciplina.getId(),
                        disciplina.getNome(),
                        disciplina.getCodigo(),
                        curso.getId(),
                        curso.getNome(),
                        totalHorasUtilizadas,
                        totalHorasDisponiveis,
                        taxaOcupacao,
                        turmasUtilizadas.size()
                    );

                ocupacaoPorDisciplina.add(ocupacao);
            }
        }

        return ocupacaoPorDisciplina;
    }

    private RelatorioOcupacaoResponseDTO.ResumoOcupacaoDTO calcularResumoOcupacao(
            List<Evento> eventos, LocalDateTime[] periodoAcademico) {

        List<Sala> salasAtivas = salaRepository.findByAtivoTrue();
        Set<Long> salasUtilizadas = eventos.stream()
            .map(e -> e.getSala().getId())
            .collect(Collectors.toSet());

        int totalHorasUtilizadas = eventos.stream()
            .mapToInt(this::calcularDuracaoEmHoras)
            .sum();

        long diasPeriodo = ChronoUnit.DAYS.between(
            periodoAcademico[0].toLocalDate(),
            periodoAcademico[1].toLocalDate()) + 1;
        int totalHorasDisponiveis = salasAtivas.size() * (int) (diasPeriodo * 16);

        Set<Long> cursosUtilizados = eventos.stream()
            .filter(e -> e.getTurma() != null && 
                        e.getTurma().getDisciplina() != null && 
                        e.getTurma().getDisciplina().getCurso() != null)
            .map(e -> e.getTurma().getDisciplina().getCurso().getId())
            .collect(Collectors.toSet());

        Set<Long> disciplinasUtilizadas = eventos.stream()
            .filter(e -> e.getTurma() != null && e.getTurma().getDisciplina() != null)
            .map(e -> e.getTurma().getDisciplina().getId())
            .collect(Collectors.toSet());

        Set<Long> turmasUtilizadas = eventos.stream()
            .filter(e -> e.getTurma() != null)
            .map(e -> e.getTurma().getId())
            .collect(Collectors.toSet());

        BigDecimal taxaOcupacaoGeral = calcularTaxaOcupacao(totalHorasUtilizadas, totalHorasDisponiveis);

        return new RelatorioOcupacaoResponseDTO.ResumoOcupacaoDTO(
            salasAtivas.size(),
            salasUtilizadas.size(),
            taxaOcupacaoGeral,
            cursosUtilizados.size(),
            disciplinasUtilizadas.size(),
            turmasUtilizadas.size(),
            totalHorasUtilizadas,
            totalHorasDisponiveis
        );
    }

    private List<RelatorioOcupacaoResponseDTO.DetalheOcupacaoDTO> gerarDetalhesOcupacaoPorSala(
            Long salaId, List<Evento> eventosSala) {

        return eventosSala.stream()
            .map(evento -> {
                var turma = evento.getTurma();
                var disciplina = turma != null ? turma.getDisciplina() : null;
                var curso = disciplina != null ? disciplina.getCurso() : null;

                return new RelatorioOcupacaoResponseDTO.DetalheOcupacaoDTO(
                    evento.getId(),
                    evento.getTitulo(),
                    turma != null ? turma.getId() : null,
                    turma != null ? turma.getCodigo() : null,
                    disciplina != null ? disciplina.getId() : null,
                    disciplina != null ? disciplina.getNome() : null,
                    curso != null ? curso.getId() : null,
                    curso != null ? curso.getNome() : null,
                    calcularDuracaoEmHoras(evento),
                    evento.getDataInicio().toString(),
                    evento.getDataFim().toString()
                );
            })
            .collect(Collectors.toList());
    }

    private int calcularDuracaoEmHoras(Evento evento) {
        Duration duration = Duration.between(evento.getDataInicio(), evento.getDataFim());
        return (int) duration.toHours();
    }

    private BigDecimal calcularTaxaOcupacao(int horasUtilizadas, int horasDisponiveis) {
        if (horasDisponiveis == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal taxa = BigDecimal.valueOf(horasUtilizadas)
            .divide(BigDecimal.valueOf(horasDisponiveis), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
            
        return taxa.setScale(2, RoundingMode.HALF_UP);
    }
}
