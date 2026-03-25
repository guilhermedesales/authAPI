package com.api.auth.Domain.Entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class UsuarioSistema {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "sistema_id")
    private Sistema sistema;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    public UsuarioSistema() {}

    public UsuarioSistema(Sistema sistema, Usuario usuario, Role role) {
        this.sistema = sistema;
        this.usuario = usuario;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Sistema getSistema() {
        return sistema;
    }

    public void setSistema(Sistema sistema) {
        this.sistema = sistema;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
