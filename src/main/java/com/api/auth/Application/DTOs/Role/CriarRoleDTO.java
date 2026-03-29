package com.api.auth.Application.DTOs.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CriarRoleDTO {

    private UUID sistemaId;
    private String nome;
    private String descricao;

}
