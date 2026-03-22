package com.api.auth.Application.DTOs.Permissao;

import java.util.UUID;

public class PermissaoDTO {

    private UUID id;
    private String nome;

    private UUID roleId;
    private String roleNome;

    public PermissaoDTO(UUID id, String nome, UUID roleId, String roleNome) {
        this.id = id;
        this.nome = nome;
        this.roleId = roleId;
        this.roleNome = roleNome;
    }

    public  UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public UUID getRoleId() {return roleId;}
    public void setRoleId(UUID roleId) {this.roleId = roleId;}

    public String getRoleNome() {return roleNome;}
    public void setRoleNome(String roleNome) {this.roleNome = roleNome;}

}
