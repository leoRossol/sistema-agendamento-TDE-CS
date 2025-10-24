package com.sistema.agendamento.sistema_agendamento.dto.Usuario;

public class LoginResponseDTO {
    private String token;
    private String mensagem;
    private Long id;
    private String email;

    public String getToken() { return token; }
    public String getMensagem() { return mensagem; }
    public Long getId() { return id; }
    public String getEmail() { return email; }

    public void setToken(String gerarToken) { this.token = gerarToken; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
}
