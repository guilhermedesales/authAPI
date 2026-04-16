package com.api.auth.Application.Service.RBACServices;

import com.api.auth.Application.Utils.RoleNames;
import com.api.auth.Domain.Entities.Role;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Infra.Repositories.RoleRepository;
import com.api.auth.Infra.Repositories.SistemaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RbacBootstrapServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private SistemaRepository sistemaRepository;

    @Test
    void shouldCreateDefaultSystemWhenMissing() {
        RbacBootstrapService rbacBootstrapService = new RbacBootstrapService(
                roleRepository,
                sistemaRepository,
                "DEFAULT",
                "Sistema padrao para bootstrap inicial da plataforma"
        );

        when(sistemaRepository.findByNomeIgnoreCase("DEFAULT")).thenReturn(Optional.empty());
        when(sistemaRepository.save(any(Sistema.class))).thenAnswer(invocation -> {
            Sistema sistema = invocation.getArgument(0);
            sistema.setId(UUID.randomUUID());
            return sistema;
        });

        when(roleRepository.findByNomeAndSistemaId(eq(RoleNames.USER), any(UUID.class))).thenReturn(Optional.empty());
        when(roleRepository.findByNomeAndSistemaId(eq(RoleNames.ADMIN), any(UUID.class))).thenReturn(Optional.empty());
        when(roleRepository.findByNomeAndSistemaIsNull(RoleNames.GLOBAL_ADMIN)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sistema defaultSystem = rbacBootstrapService.ensureDefaultSystem();

        assertNotNull(defaultSystem.getId());
        assertEquals("DEFAULT", defaultSystem.getNome());
        verify(sistemaRepository).save(any(Sistema.class));
        verify(roleRepository).findByNomeAndSistemaId(RoleNames.USER, defaultSystem.getId());
        verify(roleRepository).findByNomeAndSistemaId(RoleNames.ADMIN, defaultSystem.getId());
        verify(roleRepository).findByNomeAndSistemaIsNull(RoleNames.GLOBAL_ADMIN);
    }

    @Test
    void shouldReturnProvidedSystemWhenIdExists() {
        RbacBootstrapService rbacBootstrapService = new RbacBootstrapService(
                roleRepository,
                sistemaRepository,
                "DEFAULT",
                "Sistema padrao para bootstrap inicial da plataforma"
        );

        UUID sistemaId = UUID.randomUUID();
        Sistema sistema = Sistema.builder().id(sistemaId).nome("Tenant A").build();
        when(sistemaRepository.findById(sistemaId)).thenReturn(Optional.of(sistema));

        Sistema resolved = rbacBootstrapService.resolveSystemOrDefault(sistemaId);

        assertEquals(sistemaId, resolved.getId());
        verify(sistemaRepository, never()).findByNomeIgnoreCase(any(String.class));
    }
}

