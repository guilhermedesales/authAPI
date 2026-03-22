package com.api.auth.Domain.Entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Permissao {

    @Id
    @GeneratedValue
    private UUID id;
    private String nome;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    public Permissao() {}

    public Permissao(String nome, Role roleId) {
        this.nome = nome;
        this.role = roleId;
    }

    public UUID getId() {return id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public Role getRole() {return role;}
    public void setRole(Role role) {this.role = role;}

}
