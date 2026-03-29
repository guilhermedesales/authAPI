package com.api.auth.Application.DTOs.Permissao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PermissaoDTO {

    private UUID id;
    private String nome;
    private String descricao;

    private UUID roleId;
    private String roleNome;

    private UUID sistemaId;
    private String sistemaNome;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
