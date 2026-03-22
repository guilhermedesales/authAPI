package com.api.auth.Application.DTOs.Sistema;

import java.util.UUID;

public class SistemaDTO {

    private UUID id;
    private String nome;

    public SistemaDTO(UUID id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public UUID getId() {return id;}
    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

}
