package com.sistema.agendamento.sistema_agendamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para resposta do relatório de ocupação de salas")
public class RelatorioOcupacaoResponseDTO {
    
    @Schema(description = "Período acadêmico do relatório", example = "2025/2")
    private String periodo;
    
    @Schema(description = "Lista de dados de ocupação por sala")
    private List<OcupacaoPorSalaDTO> ocupacaoPorSala;
    
    @Schema(description = "Lista de dados de ocupação por curso")
    private List<OcupacaoPorCursoDTO> ocupacaoPorCurso;
    
    @Schema(description = "Lista de dados de ocupação por disciplina")
    private List<OcupacaoPorDisciplinaDTO> ocupacaoPorDisciplina;
    
    @Schema(description = "Resumo geral da ocupação")
    private ResumoOcupacaoDTO resumo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalhes da ocupação de uma sala específica")
    public static class OcupacaoPorSalaDTO {
        @Schema(description = "ID da sala", example = "1")
        private Long salaId;
        @Schema(description = "Nome da sala", example = "Sala A1")
        private String salaNome;
        @Schema(description = "Número da sala", example = "A1")
        private String salaNumero;
        @Schema(description = "Capacidade da sala", example = "50")
        private Integer capacidade;
        @Schema(description = "Total de horas utilizadas na sala", example = "320")
        private Integer totalHorasUtilizadas;
        @Schema(description = "Total de horas disponíveis na sala", example = "1280")
        private Integer totalHorasDisponiveis;
        @Schema(description = "Taxa de ocupação da sala em porcentagem", example = "25.00")
        private BigDecimal taxaOcupacao;
        @Schema(description = "Detalhes dos eventos que ocuparam a sala")
        private List<DetalheOcupacaoDTO> detalhes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Ocupação consolidada por curso")
    public static class OcupacaoPorCursoDTO {
        @Schema(description = "ID do curso", example = "1")
        private Long cursoId;
        @Schema(description = "Nome do curso", example = "Ciência da Computação")
        private String cursoNome;
        @Schema(description = "Código do curso", example = "CC")
        private String cursoCodigo;
        @Schema(description = "Total de horas utilizadas pelo curso", example = "640")
        private Integer totalHorasUtilizadas;
        @Schema(description = "Total de horas disponíveis para o curso", example = "2560")
        private Integer totalHorasDisponiveis;
        @Schema(description = "Taxa de ocupação do curso em porcentagem", example = "25.00")
        private BigDecimal taxaOcupacao;
        @Schema(description = "Número de salas utilizadas pelo curso", example = "2")
        private Integer totalSalasUtilizadas;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Ocupação consolidada por disciplina")
    public static class OcupacaoPorDisciplinaDTO {
        @Schema(description = "ID da disciplina", example = "101")
        private Long disciplinaId;
        @Schema(description = "Nome da disciplina", example = "Algoritmos e Estruturas de Dados")
        private String disciplinaNome;
        @Schema(description = "Código da disciplina", example = "ALG001")
        private String disciplinaCodigo;
        @Schema(description = "ID do curso ao qual a disciplina pertence", example = "1")
        private Long cursoId;
        @Schema(description = "Nome do curso ao qual a disciplina pertence", example = "Ciência da Computação")
        private String cursoNome;
        @Schema(description = "Total de horas utilizadas pela disciplina", example = "320")
        private Integer totalHorasUtilizadas;
        @Schema(description = "Total de horas disponíveis para a disciplina", example = "1280")
        private Integer totalHorasDisponiveis;
        @Schema(description = "Taxa de ocupação da disciplina em porcentagem", example = "25.00")
        private BigDecimal taxaOcupacao;
        @Schema(description = "Número de turmas da disciplina no período", example = "1")
        private Integer totalTurmas;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalhes de um evento que contribui para a ocupação")
    public static class DetalheOcupacaoDTO {
        @Schema(description = "ID do evento", example = "10")
        private Long eventoId;
        @Schema(description = "Título do evento", example = "Aula de Algoritmos")
        private String eventoTitle;
        @Schema(description = "ID da turma", example = "1")
        private Long turmaId;
        @Schema(description = "Código da turma", example = "ALG-2025-2")
        private String turmaCodigo;
        @Schema(description = "ID da disciplina", example = "101")
        private Long disciplinaId;
        @Schema(description = "Nome da disciplina", example = "Algoritmos e Estruturas de Dados")
        private String disciplinaNome;
        @Schema(description = "ID do curso", example = "1")
        private Long cursoId;
        @Schema(description = "Nome do curso", example = "Ciência da Computação")
        private String cursoNome;
        @Schema(description = "Duração do evento em horas", example = "4")
        private Integer duracaoHoras;
        @Schema(description = "Data e hora de início do evento", example = "2025-08-15T08:00:00")
        private String dataInicio;
        @Schema(description = "Data e hora de fim do evento", example = "2025-08-15T12:00:00")
        private String dataFim;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumo geral da ocupação de salas no período")
    public static class ResumoOcupacaoDTO {
        @Schema(description = "Total de salas ativas", example = "10")
        private Integer totalSalas;
        @Schema(description = "Número de salas utilizadas no período", example = "5")
        private Integer salasUtilizadas;
        @Schema(description = "Taxa de ocupação geral de todas as salas ativas no período", example = "30.50")
        private BigDecimal taxaOcupacaoGeral;
        @Schema(description = "Número total de cursos com eventos no período", example = "3")
        private Integer totalCursos;
        @Schema(description = "Número total de disciplinas com eventos no período", example = "8")
        private Integer totalDisciplinas;
        @Schema(description = "Número total de turmas no período", example = "12")
        private Integer totalTurmas;
        @Schema(description = "Total de horas de eventos agendados no período", example = "960")
        private Integer totalHorasUtilizadas;
        @Schema(description = "Total de horas disponíveis em todas as salas ativas no período", example = "2560")
        private Integer totalHorasDisponiveis;
    }
}