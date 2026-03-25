package com.api.auth.Application.DTOs.UsuarioSistema;

import java.util.UUID;

public class CriarUsuarioSistemaDTO {

    private UUID sistemaId;
    private UUID roleId;
    private UUID usuarioId;

    public UUID getSistemaId() {return sistemaId;}
    public void setSistemaId(UUID sistemaId) {this.sistemaId = sistemaId;}

    public UUID getRoleId() {return roleId;}
    public void setRoleId(UUID roleId) {this.roleId = roleId;}

    public UUID getUsuarioId() {return usuarioId;}
    public void setUsuarioId(UUID usuarioId) {this.usuarioId = usuarioId;}

}
