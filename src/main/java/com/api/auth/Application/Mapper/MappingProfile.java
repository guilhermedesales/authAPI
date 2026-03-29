package com.api.auth.Application.Mapper;

import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarResponseDTO;
import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;
import com.api.auth.Application.DTOs.Permissao.PermissaoResumoDTO;
import com.api.auth.Application.DTOs.Role.RoleDTO;
import com.api.auth.Application.DTOs.Role.RoleListDTO;
import com.api.auth.Application.DTOs.Role.RoleResumoDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaListDTO;
import com.api.auth.Application.DTOs.UsuarioSistema.CriarUsuarioSistemaDTO;
import com.api.auth.Application.DTOs.UsuarioSistema.UsuarioSistemaDTO;
import com.api.auth.Domain.Entities.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MappingProfile {

    /////////// PERMISSÃO /////////////

    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "role.nome", target = "roleNome")
    @Mapping(source = "role.sistema.id", target = "sistemaId")
    @Mapping(source = "role.sistema.nome", target = "sistemaNome")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    PermissaoDTO toDTO(Permissao permissao);

    PermissaoResumoDTO toResumoDTO(Permissao permissao);

    /////////// ROLE /////////////

    @Mapping(source = "sistema.id", target = "sistemaId")
    @Mapping(source = "sistema.nome", target = "sistemaNome")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    RoleDTO toDTO(Role role);

    @Mapping(source = "sistema.id", target = "sistemaId")
    @Mapping(source = "sistema.nome", target = "sistemaNome")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    RoleListDTO toListDTO(Role role);

    RoleResumoDTO toResumoDTO(Role role);

    /////////// SISTEMA /////////////

    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    SistemaDTO toDTO(Sistema sistema);
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    SistemaListDTO toListDTO(Sistema sistema);

    /////////// USUARIO-SISTEMA /////////////

    @Mapping(source = "sistema.id", target = "sistemaId")
    @Mapping(source = "sistema.nome", target = "sistemaNome")
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.nome", target = "usuarioNome")
    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "role.nome", target = "roleNome")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    UsuarioSistemaDTO toDTO(UsuarioSistema usuarioSistema);

    /////////// USUARIO //////////

    RegistrarResponseDTO toDTO(Usuario usuario);
}