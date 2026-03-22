package com.api.auth.Application.DTOs.Role;

import java.util.UUID;

public class RoleDTO {

    private UUID id;
    private String nome;

    private UUID sistemaId;
    private String sistemaNome;

    public RoleDTO(UUID id, String nome, UUID sistemaId, String sistemaNome) {
        this.id = id;
        this.nome = nome;
        this.sistemaId = sistemaId;
        this.sistemaNome = sistemaNome;
    }

    public UUID getId() {return id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public UUID getSistemaId() {return sistemaId;}
    public void setSistemaId(UUID sistemaId) {this.sistemaId = sistemaId;}

    public String getSistemaNome() {return sistemaNome;}
    public void setSistemaNome(String sistemaNome) {this.sistemaNome = sistemaNome;}

}
