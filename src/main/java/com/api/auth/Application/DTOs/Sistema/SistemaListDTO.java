package com.api.auth.Application.DTOs.Sistema;

import com.api.auth.Application.DTOs.Role.RoleDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SistemaListDTO {

    private UUID id;
    private String nome;
    private String descricao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
