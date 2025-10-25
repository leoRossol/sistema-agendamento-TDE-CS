package com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin;

public class RemoverUsuarioResponseDTO {
    private Long idUsuarioRemovido;
    private String mensagem;

    public Long getIdUsuarioRemovido() { return idUsuarioRemovido; }
    public void setIdUsuarioRemovido(Long idUsuarioRemovido) { this.idUsuarioRemovido = idUsuarioRemovido; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
