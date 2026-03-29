package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Role.CriarRoleDTO;
import com.api.auth.Application.DTOs.Role.RoleDTO;
import com.api.auth.Application.DTOs.Role.RoleListDTO;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Infra.Repositories.RoleRepository;
import com.api.auth.Infra.Repositories.SistemaRepository;
import com.api.auth.Domain.Entities.Role;
import com.api.auth.Domain.Entities.Sistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final SistemaRepository sistemaRepository;
    private final MappingProfile mappingProfile;

    public RoleService(RoleRepository roleRepository, SistemaRepository sistemaRepository, MappingProfile mappingProfile) {
        this.roleRepository = roleRepository;
        this.sistemaRepository = sistemaRepository;
        this.mappingProfile = mappingProfile;
    }

    public RoleListDTO criar(CriarRoleDTO dto){
        Sistema sistema = sistemaRepository.findById(dto.getSistemaId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));

        Role role = Role.builder()
                .sistema(sistema)
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .build();

        Role saved = roleRepository.save(role);
        return mappingProfile.toListDTO(saved);
    }

    public Page<RoleListDTO> listar(int page, int size) {

        PageRequest pageable = PageRequest.of(page, size);
        Page<Role> result = roleRepository.findAll(pageable);
        return result.map(mappingProfile::toListDTO);

    }

    public RoleDTO buscarPorId(UUID id) {
        Role role =  roleRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.ROLE_NAO_ENCONTRADA));
        return mappingProfile.toDTO(role);
    }
}
