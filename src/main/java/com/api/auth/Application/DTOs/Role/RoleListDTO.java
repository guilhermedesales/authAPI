package com.api.auth.Application.DTOs.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RoleListDTO {

    private UUID id;
    private String nome;
    private String descricao;

    private UUID sistemaId;
    private String sistemaNome;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
