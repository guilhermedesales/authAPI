package com.api.auth.Application.DTOs.Usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UsuarioDTO {

    private UUID id;
    private String nome;
    private String email;
    private boolean ativo;

    private boolean bloqueado;
    private LocalDateTime bloqueadoAte;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
