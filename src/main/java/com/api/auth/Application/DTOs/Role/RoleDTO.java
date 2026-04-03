package com.api.auth.Application.DTOs.Role;

import com.api.auth.Application.DTOs.Permissao.PermissaoResumoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RoleDTO {

    private UUID id;
    private String nome;
    private String descricao;

    private UUID sistemaId;
    private String sistemaNome;

    private List<PermissaoResumoDTO> permissoes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
