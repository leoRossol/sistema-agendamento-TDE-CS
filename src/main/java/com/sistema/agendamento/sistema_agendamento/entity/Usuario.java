package com.sistema.agendamento.sistema_agendamento.entity;

import jakarta.persistence.*;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "usuarios")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nome;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String senha;

    @Column(unique = true)
    private int matricula;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false)
    private TipoUsuario tipoUsuario;
    
    @Column(columnDefinition = "boolean default true")
    private Boolean ativo = true;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Notificacao> notificacoes;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Evento> eventos;

    public Usuario(String nome, String email, String senha, TipoUsuario tipoUsuario, int matricula) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
        this.matricula = matricula;
        this.ativo = true;

        eventos = new ArrayList<>();
        notificacoes = new ArrayList<>();
    }

    public Usuario() { }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public void ativar() { this.ativo = true; }
    public void desativar() { this.ativo = false; }
    public boolean isAtivo() { return this.ativo != null && this.ativo; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public Long getId() { return id; } 
    public String getSenha() { return senha; }
    public int getMatricula() { return matricula; }
    public void setSenha(String senha) { this.senha = senha; }
    public List<Notificacao> getNotificacoes() { return notificacoes; }
    public List<Evento> getEventos() { return eventos; }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", tipoUsuario=" + tipoUsuario +
                ", matricula=" + matricula +
                ", ativo=" + ativo +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}