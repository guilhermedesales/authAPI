package com.api.auth.Application.Service.RBACServices;

import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Application.Utils.RoleNames;
import com.api.auth.Domain.Entities.Role;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Infra.Repositories.RoleRepository;
import com.api.auth.Infra.Repositories.SistemaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RbacBootstrapService {

    private static final String FALLBACK_DEFAULT_SYSTEM_NAME = "DEFAULT";
    private static final String FALLBACK_DEFAULT_SYSTEM_DESCRIPTION = "Sistema padrao para bootstrap inicial da plataforma";

    private final RoleRepository roleRepository;
    private final SistemaRepository sistemaRepository;
    private final String defaultSystemName;
    private final String defaultSystemDescription;

    public RbacBootstrapService(RoleRepository roleRepository,
                                SistemaRepository sistemaRepository,
                                @Value("${auth.default-system.name:DEFAULT}") String defaultSystemName,
                                @Value("${auth.default-system.description:Sistema padrao para bootstrap inicial da plataforma}") String defaultSystemDescription) {
        this.roleRepository = roleRepository;
        this.sistemaRepository = sistemaRepository;
        this.defaultSystemName = sanitize(defaultSystemName, FALLBACK_DEFAULT_SYSTEM_NAME);
        this.defaultSystemDescription = sanitize(defaultSystemDescription, FALLBACK_DEFAULT_SYSTEM_DESCRIPTION);
    }

    @Transactional
    public void ensureDefaultsForAllSystems() {
        ensureDefaultSystem();
        ensureGlobalAdminRole();
        sistemaRepository.findAll().forEach(this::ensureSystemRoles);
    }

    @Transactional
    public Sistema ensureDefaultSystem() {

        Sistema defaultSystem = sistemaRepository.findByNomeIgnoreCase(defaultSystemName)
                .orElseGet(() -> sistemaRepository.save(Sistema.builder()
                        .nome(defaultSystemName)
                        .descricao(defaultSystemDescription)
                        .build()));

        ensureSystemRoles(defaultSystem);
        return defaultSystem;
    }

    @Transactional
    public Sistema resolveSystemOrDefault(UUID sistemaId) {
        if (sistemaId != null ) {
            return sistemaRepository.findById(sistemaId)
                    .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));
        }
        if (sistemaRepository.count() <= 1){ // cai no default ate ter outro sistema
            return ensureDefaultSystem();
        }
        throw new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO);
    }

    @Transactional
    public void ensureSystemRoles(Sistema sistema) {
        getOrCreateUserRole(sistema);
        getOrCreateAdminRole(sistema);
        ensureGlobalAdminRole();
    }

    @Transactional
    public Role getOrCreateUserRole(Sistema sistema) {
        return roleRepository.findByNomeAndSistemaId(RoleNames.USER, sistema.getId())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .nome(RoleNames.USER)
                        .descricao("Role padrao para novos usuarios do sistema")
                        .sistema(sistema)
                        .build()));
    }

    @Transactional
    public Role getOrCreateAdminRole(Sistema sistema) {
        return roleRepository.findByNomeAndSistemaId(RoleNames.ADMIN, sistema.getId())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .nome(RoleNames.ADMIN)
                        .descricao("Administrador do sistema")
                        .sistema(sistema)
                        .build()));
    }

    @Transactional
    public Role ensureGlobalAdminRole() {
        return roleRepository.findByNomeAndSistemaIsNull(RoleNames.GLOBAL_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .nome(RoleNames.GLOBAL_ADMIN)
                        .descricao("Administrador global da plataforma")
                        .sistema(null)
                        .build()));
    }

    private String sanitize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

}


