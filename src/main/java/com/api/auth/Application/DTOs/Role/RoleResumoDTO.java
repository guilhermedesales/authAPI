package com.api.auth.Application.DTOs.Role;

import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RoleResumoDTO {

    private UUID id;
    private String nome;
    private String descricao;


}
