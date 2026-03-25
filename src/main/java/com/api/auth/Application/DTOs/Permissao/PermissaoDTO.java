package com.api.auth.Application.DTOs.Permissao;

import java.util.UUID;

public class PermissaoDTO {

    private UUID id;
    private String nome;
    private String descricao;

    private UUID roleId;
    private String roleNome;

    private UUID sistemaId;
    private String sistemaNome;

    public PermissaoDTO(UUID id, String nome, String descricao, UUID roleId, String roleNome, UUID sistemaId, String sistemaNome) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.roleId = roleId;
        this.roleNome = roleNome;
        this.sistemaId = sistemaId;
        this.sistemaNome = sistemaNome;
    }

    public  UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public UUID getRoleId() {return roleId;}
    public void setRoleId(UUID roleId) {this.roleId = roleId;}

    public String getRoleNome() {return roleNome;}
    public void setRoleNome(String roleNome) {this.roleNome = roleNome;}

    public String getDescricao() {return descricao;}
    public void setDescricao(String descricao) {this.descricao = descricao;}

    public UUID getSistemaId() {return sistemaId;}
    public void setSistemaId(UUID sistemaId) {this.sistemaId = sistemaId;}

    public String getSistemaNome() {return sistemaNome;}
    public void setSistemaNome(String sistemaNome) {this.sistemaNome = sistemaNome;}

}
