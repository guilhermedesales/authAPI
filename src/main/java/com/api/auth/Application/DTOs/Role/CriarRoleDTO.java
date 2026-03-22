package com.api.auth.Application.DTOs.Role;

import java.util.UUID;

public class CriarRoleDTO {

    private String nome;

    private UUID sistemaId;

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public UUID getSistemaId() {return sistemaId;}
    public void setSistemaId(UUID sistemaId) {this.sistemaId = sistemaId;}

}
