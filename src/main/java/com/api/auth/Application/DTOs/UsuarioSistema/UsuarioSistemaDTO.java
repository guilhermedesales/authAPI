package com.api.auth.Application.DTOs.UsuarioSistema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UsuarioSistemaDTO {

    private UUID id;
    private UUID sistemaId;
    private String sistemaNome;
    private UUID usuarioId;
    private String usuarioNome;
    private UUID roleId;
    private String roleNome;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
