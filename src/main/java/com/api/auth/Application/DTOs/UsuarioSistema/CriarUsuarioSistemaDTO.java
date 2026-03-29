package com.api.auth.Application.DTOs.UsuarioSistema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CriarUsuarioSistemaDTO {

    private UUID sistemaId;
    private UUID roleId;
    private UUID usuarioId;

}
