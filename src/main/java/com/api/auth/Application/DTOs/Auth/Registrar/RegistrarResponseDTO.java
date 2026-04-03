package com.api.auth.Application.DTOs.Auth.Registrar;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrarResponseDTO {

    private UUID id;
    private String nome;
    private String email;
    private boolean emailConfirmado;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
