package com.sistema.agendamento.sistema_agendamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para requisição de relatório de ocupação de salas")
public class RelatorioOcupacaoRequestDTO {

    @NotBlank(message = "Período é obrigatório")
    @Pattern(regexp = "^\\d{4}/[1-2]$", message = "Período deve estar no formato YYYY/S (ex: 2025/2)")
    @Schema(description = "Período acadêmico no formato YYYY/S (ex: 2025/2)", example = "2025/2", required = true)
    private String periodo;

    @Schema(description = "ID do curso para filtrar o relatório", example = "1")
    private Long cursoId;

    @Schema(description = "ID da disciplina para filtrar o relatório", example = "101")
    private Long disciplinaId;

    @Schema(description = "ID da sala para filtrar o relatório", example = "5")
    private Long salaId;
}
