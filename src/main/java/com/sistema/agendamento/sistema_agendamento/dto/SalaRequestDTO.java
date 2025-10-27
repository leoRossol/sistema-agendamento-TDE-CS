package com.sistema.agendamento.sistema_agendamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Request DTO para alocação de sala")
public class SalaRequestDTO {
    
    @Schema(description = "ID da turma", required = true, example = "1")
    @NotNull private Long turmaId;
    
    @Schema(description = "ID da sala", required = true, example = "10")
    @NotNull private Long salaId;
    
    @Schema(description = "Data e hora de início da alocação", required = true, example = "2025-10-01T08:00:00")
    @NotNull private LocalDateTime inicio;
    
    @Schema(description = "Data e hora de fim da alocação", required = true, example = "2025-10-01T10:00:00")
    @NotNull private LocalDateTime fim;
    
    @Schema(description = "Mapa de equipamentos necessários (equipamentoId -> quantidade)", example = "{\"1\": 5, \"2\": 2}")
    private Map<Long, Integer> equipamentos;

    public SalaRequestDTO() {}

    public SalaRequestDTO(Long turmaId, Long salaId, LocalDateTime inicio, LocalDateTime fim, Map<Long,Integer> equipamentos) {
        this.turmaId = turmaId;
        this.salaId = salaId;
        this.inicio = inicio;
        this.fim = fim;
        this.equipamentos = equipamentos;
    }

    // getters/setters para Jackson
    public Long getTurmaId() { return turmaId; }
    public void setTurmaId(Long turmaId) { this.turmaId = turmaId; }
    public Long getSalaId() { return salaId; }
    public void setSalaId(Long salaId) { this.salaId = salaId; }
    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public LocalDateTime getFim() { return fim; }
    public void setFim(LocalDateTime fim) { this.fim = fim; }
    public Map<Long, Integer> getEquipamentos() { return equipamentos; }
    public void setEquipamentos(Map<Long, Integer> equipamentos) { this.equipamentos = equipamentos; }

    // aliases no estilo record para não precisar mudar o service
    public Long turmaId() { return turmaId; }
    public Long salaId() { return salaId; }
    public LocalDateTime inicio() { return inicio; }
    public LocalDateTime fim() { return fim; }
    public Map<Long,Integer> equipamentos() { return equipamentos; }
}