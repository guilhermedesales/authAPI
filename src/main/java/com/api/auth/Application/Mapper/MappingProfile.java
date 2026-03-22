package com.api.auth.Application.Mapper;

import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;
import com.api.auth.Application.DTOs.Role.RoleDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaDTO;
import com.api.auth.Domain.Entities.Permissao;
import com.api.auth.Domain.Entities.Role;
import com.api.auth.Domain.Entities.Sistema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MappingProfile {

    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "role.nome", target = "roleNome")
    PermissaoDTO toDTO(Permissao permissao);

    @Mapping(source = "sistema.id", target = "sistemaId")
    @Mapping(source = "sistema.nome", target = "sistemaNome")
    RoleDTO toDTO(Role role);

    SistemaDTO toDTO(Sistema sistema);
}