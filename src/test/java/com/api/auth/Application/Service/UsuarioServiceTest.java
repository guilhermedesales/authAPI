package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Usuario.AtualizarUsuarioDTO;
import com.api.auth.Application.DTOs.Usuario.UsuarioDTO;
import com.api.auth.Application.Exceptions.ForbiddenException;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Application.Service.RBACServices.RbacAuthorizationService;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private MappingProfile mappingProfile;
    @Mock
    private RbacAuthorizationService rbacAuthorizationService;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, mappingProfile, rbacAuthorizationService);
    }

    @Test
    void listarAdminComSistemaDiferenteDeveNegarAcesso() {
        UUID sistemaToken = UUID.randomUUID();
        UUID sistemaParam = UUID.randomUUID();

        when(rbacAuthorizationService.isGlobalAdmin()).thenReturn(false);
        when(rbacAuthorizationService.getCurrentSistemaId()).thenReturn(sistemaToken);

        assertThrows(ForbiddenException.class, () -> usuarioService.listar(sistemaParam, 0, 10));
        verify(usuarioRepository, never()).findAll(any(PageRequest.class));
        verify(usuarioRepository, never()).findBySistemaId(any(UUID.class), any(PageRequest.class));
    }

    @Test
    void listarAdminSemFiltroDeveForcarSistemaDoToken() {
        UUID sistemaToken = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(rbacAuthorizationService.isGlobalAdmin()).thenReturn(false);
        when(rbacAuthorizationService.getCurrentSistemaId()).thenReturn(sistemaToken);
        when(usuarioRepository.findBySistemaId(eq(sistemaToken), any(PageRequest.class))).thenReturn(page);
        when(mappingProfile.toUserDTO(usuario)).thenReturn(new UsuarioDTO(usuario.getId(), "Nome", "mail@mail.com", true, false, null, null, null));

        Page<UsuarioDTO> response = usuarioService.listar(null, 0, 10);

        assertEquals(1, response.getTotalElements());
        verify(usuarioRepository).findBySistemaId(eq(sistemaToken), any(PageRequest.class));
        verify(usuarioRepository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void buscarPorIdUsuarioComumPodeVerProprioRegistro() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(rbacAuthorizationService.isGlobalAdmin()).thenReturn(false);
        when(rbacAuthorizationService.getCurrentUsuarioId()).thenReturn(usuarioId);
        when(mappingProfile.toUserDTO(usuario)).thenReturn(new UsuarioDTO(usuarioId, "Nome", "mail@mail.com", true, false, null, null, null));

        UsuarioDTO response = usuarioService.buscarPorId(usuarioId);

        assertEquals(usuarioId, response.getId());
    }

    @Test
    void buscarPorIdUsuarioComumNaoPodeVerOutroUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID outroUsuarioId = UUID.randomUUID();
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(outroUsuarioId);

        when(usuarioRepository.findById(outroUsuarioId)).thenReturn(Optional.of(outroUsuario));
        when(rbacAuthorizationService.isGlobalAdmin()).thenReturn(false);
        when(rbacAuthorizationService.getCurrentUsuarioId()).thenReturn(usuarioId);
        when(rbacAuthorizationService.isAdmin()).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> usuarioService.buscarPorId(outroUsuarioId));
    }

    @Test
    void atualizarAdminDoMesmoSistemaDevePermitir() {
        UUID sistemaToken = UUID.randomUUID();
        UUID usuarioAlvoId = UUID.randomUUID();
        UUID usuarioAdminId = UUID.randomUUID();

        Usuario usuarioAlvo = new Usuario();
        usuarioAlvo.setId(usuarioAlvoId);
        usuarioAlvo.setNome("Antes");

        AtualizarUsuarioDTO dto = new AtualizarUsuarioDTO();
        dto.setNome("Depois");

        when(usuarioRepository.findById(usuarioAlvoId)).thenReturn(Optional.of(usuarioAlvo));
        when(rbacAuthorizationService.isGlobalAdmin()).thenReturn(false);
        when(rbacAuthorizationService.getCurrentUsuarioId()).thenReturn(usuarioAdminId);
        when(rbacAuthorizationService.isAdmin()).thenReturn(true);
        when(rbacAuthorizationService.getCurrentSistemaId()).thenReturn(sistemaToken);
        when(usuarioRepository.existsByIdAndSistemaId(usuarioAlvoId, sistemaToken)).thenReturn(true);
        when(usuarioRepository.save(usuarioAlvo)).thenReturn(usuarioAlvo);
        when(mappingProfile.toUserDTO(usuarioAlvo)).thenReturn(new UsuarioDTO(usuarioAlvoId, "Depois", "mail@mail.com", true, false, null, null, null));

        UsuarioDTO response = usuarioService.atualizar(usuarioAlvoId, dto);

        assertEquals("Depois", response.getNome());
        verify(usuarioRepository).save(usuarioAlvo);
    }
}

