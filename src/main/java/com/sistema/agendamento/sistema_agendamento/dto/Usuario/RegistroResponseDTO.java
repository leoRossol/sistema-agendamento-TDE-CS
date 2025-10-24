package com.sistema.agendamento.sistema_agendamento.dto.Usuario;

public class RegistroResponseDTO {
    private String mensagem;
    private Long id;
    
    public String getMensagem() { return mensagem; }
    public Long getId() { return id; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public void setId(Long id) { this.id = id; }
}
