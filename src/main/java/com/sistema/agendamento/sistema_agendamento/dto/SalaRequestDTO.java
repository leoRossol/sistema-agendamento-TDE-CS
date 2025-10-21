package com.sistema.agendamento.sistema_agendamento.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

public class SalaRequestDTO {
    @NotNull private Long turmaId;
    @NotNull private Long salaId;
    @NotNull private LocalDateTime inicio;
    @NotNull private LocalDateTime fim;
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