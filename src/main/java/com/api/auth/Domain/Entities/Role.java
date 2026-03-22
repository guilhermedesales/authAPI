package com.api.auth.Domain.Entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Role {

    @Id
    @GeneratedValue
    private UUID id;
    private String nome;

    @ManyToOne
    @JoinColumn(name = "sistema_id")
    private Sistema sistema;

    public Role() {}

    public Role( String nome, Sistema sistema) {
        this.nome = nome;
        this.sistema = sistema;
    }

    public UUID getId() {return id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public Sistema getSistema() {return sistema;}
    public void setSistema(Sistema sistema) {this.sistema = sistema;}

}
