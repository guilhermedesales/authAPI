package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Usuario.AtualizarUsuarioDTO;
import com.api.auth.Application.DTOs.Usuario.UsuarioDTO;
import com.api.auth.Application.Exceptions.ForbiddenException;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Application.Service.RBACServices.RbacAuthorizationService;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import com.api.auth.Domain.Entities.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final MappingProfile mappingProfile;
    private final RbacAuthorizationService rbacAuthorizationService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          MappingProfile mappingProfile,
                          RbacAuthorizationService rbacAuthorizationService) {
        this.usuarioRepository = usuarioRepository;
        this.mappingProfile = mappingProfile;
        this.rbacAuthorizationService = rbacAuthorizationService;
    }

    public Page<UsuarioDTO> listar(UUID sistemaId, int page, int size) {
        log.debug("[USUARIO] List started - page={} size={}", page, size);

        PageRequest pageable = PageRequest.of(page, size);

        Page<Usuario> result;

        if (!rbacAuthorizationService.isGlobalAdmin()) {
            UUID currentSistemaId = rbacAuthorizationService.getCurrentSistemaId();
            if (currentSistemaId == null) {
                throw new ForbiddenException(ErrorMessages.Auth.ACESSO_NEGADO_SISTEMA);
            }

            if (sistemaId != null && !currentSistemaId.equals(sistemaId)) {
                throw new ForbiddenException(ErrorMessages.Auth.ACESSO_NEGADO_SISTEMA);
            }

            sistemaId = currentSistemaId;
        }

        if(sistemaId != null) // filtrou por sistema
            result = usuarioRepository.findBySistemaId(sistemaId,  pageable);

        else
            result = usuarioRepository.findAll(pageable);

        log.debug("[USUARIO] List success - totalElements={}", result.getTotalElements());
        return result.map(mappingProfile::toUserDTO);
    }

    public UsuarioDTO buscarPorId(UUID id) {
        log.debug("[USUARIO] Find by id started - usuarioId={}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO));
        validateCanAccessUsuario(usuario);
        log.debug("[USUARIO] Find by id success - usuarioId={}", usuario.getId());
        return mappingProfile.toUserDTO(usuario);
    }

    @Transactional
    public UsuarioDTO atualizar(UUID id, AtualizarUsuarioDTO dto) {
        log.info("[USUARIO] Update started - usuarioId={}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO));

        validateCanAccessUsuario(usuario);
        usuario.setNome(dto.getNome().trim());

        Usuario updated = usuarioRepository.save(usuario);
        log.info("[USUARIO] Update success - usuarioId={}", updated.getId());
        return mappingProfile.toUserDTO(updated);
    }

    private void validateCanAccessUsuario(Usuario usuario) {
        if (rbacAuthorizationService.isGlobalAdmin()) {
            return;
        }

        UUID currentUsuarioId = rbacAuthorizationService.getCurrentUsuarioId();
        if (usuario.getId().equals(currentUsuarioId)) {
            return;
        }

        if (rbacAuthorizationService.isAdmin()) {
            UUID currentSistemaId = rbacAuthorizationService.getCurrentSistemaId();
            if (currentSistemaId != null && usuarioRepository.existsByIdAndSistemaId(usuario.getId(), currentSistemaId)) {
                return;
            }
        }

        throw new ForbiddenException(ErrorMessages.Auth.ACESSO_NEGADO_SISTEMA);
    }

}
