package com.api.auth.Domain.Entities;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
public class Role {

    @Id
    @GeneratedValue
    private UUID id;
    private String nome;
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "sistema_id")
    private Sistema sistema;

    @OneToMany(mappedBy = "role")
    private List<Permissao> permissoes;

    public Role() {}

    public Role( Sistema sistema, String nome, String descricao) {
        this.sistema = sistema;
        this.nome = nome;
        this.descricao = descricao;
    }

    public UUID getId() {return id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public Sistema getSistema() {return sistema;}
    public void setSistema(Sistema sistema) {this.sistema = sistema;}

    public List<Permissao> getPermissoes() {return permissoes;}
    public void setPermissoes(List<Permissao> permissoes) {this.permissoes = permissoes;}

    public String getDescricao() {return descricao;}
    public void setDescricao(String descricao) {this.descricao = descricao;}

}
