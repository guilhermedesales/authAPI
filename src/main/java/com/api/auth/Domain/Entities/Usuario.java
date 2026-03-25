package com.api.auth.Domain.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Usuario {

    @Id
    @GeneratedValue
    private UUID id;
    private String nome;
    private String email;
    private String senha;
    private boolean ativo;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public Usuario() {}

    public Usuario(String nome, String email, String senha, boolean ativo) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.ativo = ativo;
    }

    public UUID getId() {return id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getSenha() {return senha;}
    public void setSenha(String senha) {this.senha = senha;}

    public boolean isAtivo() {return ativo;}
    public void setAtivo(boolean ativo) {this.ativo = ativo;}

}
