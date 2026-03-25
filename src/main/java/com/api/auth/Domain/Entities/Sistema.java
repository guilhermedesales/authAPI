package com.api.auth.Domain.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;
import java.util.UUID;

@Entity
public class Sistema {

    @Id
    @GeneratedValue
    private UUID id;
    private String nome;
    private String descricao;

    @OneToMany(mappedBy = "sistema")
    private List<Role> roles;

    public Sistema() {}

    public Sistema(String nome, String desc) {
        this.nome = nome;
        this.descricao = desc;
    }

    public UUID getId() {return id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public String getDescricao() {return descricao;}
    public void setDescricao(String descricao) {this.descricao = descricao;}

    public List<Role> getRoles() {return roles;}
    public void setRoles(List<Role> roles) {this.roles = roles;}
}
