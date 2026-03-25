package com.api.auth.Application.DTOs.Role;

import java.util.UUID;

public class RoleListDTO {

    private UUID id;
    private String nome;
    private String descricao;

    private UUID sistemaId;
    private String sistemaNome;

    public RoleListDTO(UUID id, String nome, String descricao, UUID sistemaId, String sistemaNome) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.sistemaId = sistemaId;
        this.sistemaNome = sistemaNome;
    }

    public UUID getId() {return id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public String getDescricao() {return descricao;}
    public void setDescricao(String descricao) {this.descricao = descricao;}

    public UUID getSistemaId() {return sistemaId;}
    public void setSistemaId(UUID sistemaId) {this.sistemaId = sistemaId;}

    public String getSistemaNome() {return sistemaNome;}
    public void setSistemaNome(String sistemaNome) {this.sistemaNome = sistemaNome;}

}
