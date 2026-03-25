package com.api.auth.Application.Mapper;

import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;
import com.api.auth.Application.DTOs.Permissao.PermissaoResumoDTO;
import com.api.auth.Application.DTOs.Role.RoleDTO;
import com.api.auth.Application.DTOs.Role.RoleListDTO;
import com.api.auth.Application.DTOs.Role.RoleResumoDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaListDTO;
import com.api.auth.Application.DTOs.UsuarioSistema.CriarUsuarioSistemaDTO;
import com.api.auth.Application.DTOs.UsuarioSistema.UsuarioSistemaDTO;
import com.api.auth.Domain.Entities.Permissao;
import com.api.auth.Domain.Entities.Role;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.UsuarioSistema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MappingProfile {

    /////////// PERMISSÃO /////////////

    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "role.nome", target = "roleNome")
    @Mapping(source = "role.sistema.id", target = "sistemaId")
    @Mapping(source = "role.sistema.nome", target = "sistemaNome")
    PermissaoDTO toDTO(Permissao permissao);

    PermissaoResumoDTO toResumoDTO(Permissao permissao);

    /////////// ROLE /////////////

    @Mapping(source = "sistema.id", target = "sistemaId")
    @Mapping(source = "sistema.nome", target = "sistemaNome")
    RoleDTO toDTO(Role role);

    @Mapping(source = "sistema.id", target = "sistemaId")
    @Mapping(source = "sistema.nome", target = "sistemaNome")
    RoleListDTO toListDTO(Role role);

    RoleResumoDTO toResumoDTO(Role role);

    /////////// SISTEMA /////////////

    SistemaDTO toDTO(Sistema sistema);
    SistemaListDTO toListDTO(Sistema sistema);

    /////////// USUARIO-SISTEMA /////////////

    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "sistema.id", target = "sistemaId")
    @Mapping(source = "usuario.id", target = "usuarioId")
    UsuarioSistemaDTO toDTO(UsuarioSistema usuarioSistema);
}