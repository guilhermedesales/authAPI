package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Role.CriarRoleDTO;
import com.api.auth.Application.DTOs.Role.RoleDTO;
import com.api.auth.Application.DTOs.Role.RoleListDTO;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Application.Service.RBACServices.RbacAuthorizationService;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Infra.Repositories.RoleRepository;
import com.api.auth.Infra.Repositories.SistemaRepository;
import com.api.auth.Domain.Entities.Role;
import com.api.auth.Domain.Entities.Sistema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final SistemaRepository sistemaRepository;
    private final MappingProfile mappingProfile;
    private final RbacAuthorizationService rbacAuthorizationService;

    public RoleService(RoleRepository roleRepository,
                       SistemaRepository sistemaRepository,
                       MappingProfile mappingProfile,
                       RbacAuthorizationService rbacAuthorizationService) {
        this.roleRepository = roleRepository;
        this.sistemaRepository = sistemaRepository;
        this.mappingProfile = mappingProfile;
        this.rbacAuthorizationService = rbacAuthorizationService;
    }

    public RoleListDTO criar(CriarRoleDTO dto){
        log.info("[ROLE] Create started - sistemaId={} nome={}", dto.getSistemaId(), dto.getNome());
        Sistema sistema = sistemaRepository.findById(dto.getSistemaId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));
        rbacAuthorizationService.validateCanManageSistema(sistema.getId());

        Role role = Role.builder()
                .sistema(sistema)
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .build();

        Role saved = roleRepository.save(role);
        log.info("[ROLE] Create success - roleId={} sistemaId={}", saved.getId(), sistema.getId());
        return mappingProfile.toListDTO(saved);
    }

    public Page<RoleListDTO> listar(int page, int size) {
        log.debug("[ROLE] List started - page={} size={}", page, size);

        PageRequest pageable = PageRequest.of(page, size);
        Page<Role> result;
        if (rbacAuthorizationService.isGlobalAdmin()) {
            result = roleRepository.findAll(pageable);
        } else {
            result = roleRepository.findBySistemaId(rbacAuthorizationService.getCurrentSistemaId(), pageable);
        }
        log.debug("[ROLE] List success - totalElements={}", result.getTotalElements());
        return result.map(mappingProfile::toListDTO);

    }

    public RoleDTO buscarPorId(UUID id) {
        log.debug("[ROLE] Find by id started - roleId={}", id);
        Role role =  roleRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.ROLE_NAO_ENCONTRADA));
        rbacAuthorizationService.validateCanManageRole(role);
        log.debug("[ROLE] Find by id success - roleId={}", role.getId());
        return mappingProfile.toDTO(role);
    }
}
