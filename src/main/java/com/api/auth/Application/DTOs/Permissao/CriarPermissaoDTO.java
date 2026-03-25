package com.api.auth.Application.DTOs.Permissao;

import java.util.UUID;

public class CriarPermissaoDTO {

    private  String nome;
    private  String descricao;
    private UUID roleId;

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public UUID getRoleId() {return roleId;}
    public void setRoleId(UUID roleId) {this.roleId = roleId;}

    public String getDescricao() {return descricao;}
    public void setDescricao(String descricao) {this.descricao = descricao;}
}
