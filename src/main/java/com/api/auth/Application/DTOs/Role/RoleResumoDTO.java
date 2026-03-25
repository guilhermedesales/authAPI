package com.api.auth.Application.DTOs.Role;

import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;

import java.util.List;
import java.util.UUID;

public class RoleResumoDTO {

    private UUID id;
    private String nome;
    private String descricao;


    public RoleResumoDTO(UUID id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
    }

    public UUID getId() {return id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public String getDescricao() {return descricao;}
    public void setDescricao(String descricao) {this.descricao = descricao;}
}
