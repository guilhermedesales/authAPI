package com.api.auth.Domain.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Sistema {

    @Id
    @GeneratedValue
    private UUID id;
    private String nome;

    public Sistema() {}

    public Sistema(String nome) {
        this.nome = nome;
    }

    public UUID getId() {return id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

}
