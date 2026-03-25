package com.api.auth.Application.DTOs.UsuarioSistema;

import java.util.UUID;

public class UsuarioSistemaDTO {

    private UUID id;
    private UUID sistemaId;
    private UUID usuarioId;
    private UUID roleId;

    public UsuarioSistemaDTO(UUID id, UUID sistemaId, UUID usuarioId, UUID roleId) {
        this.id = id;
        this.sistemaId = sistemaId;
        this.usuarioId = usuarioId;
        this.roleId = roleId;
    }


    public UUID getId() { return id;}

    public UUID  getSistemaId() { return sistemaId;}
    public void setSistemaId(UUID sistemaId) {this.sistemaId = sistemaId;}

    public UUID getUsuarioId() {return usuarioId;}
    public void setUsuarioId(UUID usuarioId) {this.usuarioId = usuarioId;}

    public UUID getRoleId() {return roleId;}
    public void setRoleId(UUID roleId) {this.roleId = roleId;}
}
