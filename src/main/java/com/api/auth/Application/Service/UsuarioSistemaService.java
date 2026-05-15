package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.UsuarioSistema.CriarUsuarioSistemaDTO;
import com.api.auth.Application.DTOs.UsuarioSistema.UsuarioSistemaDTO;
import com.api.auth.Application.Exceptions.ForbiddenException;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Application.Service.RBACServices.RbacAuthorizationService;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Domain.Entities.Role;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Infra.Repositories.RoleRepository;
import com.api.auth.Infra.Repositories.SistemaRepository;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import com.api.auth.Infra.Repositories.UsuarioSistemaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class UsuarioSistemaService {

    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final SistemaRepository sistemaRepository;
    private final UsuarioRepository  usuarioRepository;
    private final RoleRepository roleRepository;
    private final MappingProfile mappingProfile;
    private final RbacAuthorizationService rbacAuthorizationService;

    public UsuarioSistemaService(UsuarioSistemaRepository usuarioSistemaRepository,
                                 SistemaRepository sistemaRepository,
                                 MappingProfile mappingProfile,
                                 UsuarioRepository usuarioRepository,
                                 RoleRepository roleRepository,
                                 RbacAuthorizationService rbacAuthorizationService) {
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.sistemaRepository = sistemaRepository;
        this.mappingProfile = mappingProfile;
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.rbacAuthorizationService = rbacAuthorizationService;
    }

    public UsuarioSistemaDTO criar(CriarUsuarioSistemaDTO dto) {
        log.info("[USUARIO_SISTEMA] Link create started - usuarioId={} sistemaId={} roleId={}",
                dto.getUsuarioId(), dto.getSistemaId(), dto.getRoleId());

        Sistema sistema = sistemaRepository.findById(dto.getSistemaId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));
        rbacAuthorizationService.validateCanManageSistema(sistema.getId());

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO));

        Role role = roleRepository.findByIdAndSistemaId(dto.getRoleId(), dto.getSistemaId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.ROLE_NAO_ENCONTRADA_SISTEMA));

        boolean existe = usuarioSistemaRepository
                .existsByUsuarioIdAndSistemaId(dto.getUsuarioId(), dto.getSistemaId());

        if (existe) {
            log.warn("[USUARIO_SISTEMA] Link create blocked - reason=already_linked usuarioId={} sistemaId={}",
                    dto.getUsuarioId(), dto.getSistemaId());
            throw new RuntimeException("Usuario já vinculado a este sistema");
        }

        UsuarioSistema usuarioSistema = UsuarioSistema.builder()
                .sistema(sistema)
                .usuario(usuario)
                .role(role)
                .build();

        UsuarioSistema saved = usuarioSistemaRepository.save(usuarioSistema);
        log.info("[USUARIO_SISTEMA] Link create success - usuarioSistemaId={}", saved.getId());
        return mappingProfile.toDTO(saved);
    }

    public Page<UsuarioSistemaDTO> listar(UUID usuarioId, UUID sistemaId, int page, int size) {
        log.debug("[USUARIO_SISTEMA] List started - usuarioId={} sistemaId={} page={} size={}",
                usuarioId, sistemaId, page, size);
        PageRequest pageable = PageRequest.of(page, size);
        Page<UsuarioSistema> lista;

        if (!rbacAuthorizationService.isGlobalAdmin()) {
            UUID currentSistemaId = rbacAuthorizationService.getCurrentSistemaId();
            if (sistemaId != null && !currentSistemaId.equals(sistemaId)) {
                throw new ForbiddenException(ErrorMessages.Auth.ACESSO_NEGADO_SISTEMA);
            }
            sistemaId = currentSistemaId;
        }

        if(usuarioId != null && sistemaId != null) // filtrou por usuario e sistema
            lista = usuarioSistemaRepository.findByUsuarioIdAndSistemaId(usuarioId, sistemaId, pageable);

        else if(usuarioId != null) // filtrou por usuario
            lista = usuarioSistemaRepository.findByUsuarioId(usuarioId, pageable);

        else if(sistemaId != null) // filtrou por sistema
            lista = usuarioSistemaRepository.findBySistemaId(sistemaId,  pageable);

        else // sem filtro
            lista = usuarioSistemaRepository.findAll(pageable);

        log.debug("[USUARIO_SISTEMA] List success - totalElements={}", lista.getTotalElements());
        return lista.map(mappingProfile::toDTO);

    }

    public UsuarioSistemaDTO buscarPorId(UUID id){
        log.debug("[USUARIO_SISTEMA] Find by id started - usuarioSistemaId={}", id);
        UsuarioSistema usuarioSistema = usuarioSistemaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));
        rbacAuthorizationService.validateCanManageSistema(usuarioSistema.getSistema().getId());
        log.debug("[USUARIO_SISTEMA] Find by id success - usuarioSistemaId={}", usuarioSistema.getId());
        return mappingProfile.toDTO(usuarioSistema);
    }

    public UsuarioSistemaDTO mudarRoleUser(UUID usuarioSistemaId, UUID novaRoleId) {
        log.info("[USUARIO_SISTEMA] Change role started - usuarioSistemaId={} novaRoleId={}", usuarioSistemaId, novaRoleId);

        UsuarioSistema usuarioSistema = usuarioSistemaRepository.findById(usuarioSistemaId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));
        rbacAuthorizationService.validateCanManageSistema(usuarioSistema.getSistema().getId());

        Role novaRole = roleRepository
                .findByIdAndSistemaId(novaRoleId, usuarioSistema.getSistema().getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.ROLE_NAO_ENCONTRADA_SISTEMA));

        usuarioSistema.setRole(novaRole);
        UsuarioSistema updated = usuarioSistemaRepository.save(usuarioSistema);

        log.info("[USUARIO_SISTEMA] Change role success - usuarioSistemaId={} roleId={}", updated.getId(), updated.getRole().getId());
        return mappingProfile.toDTO(updated);
    }

    public void remover(UUID usuarioSistemaId) {
        log.info("[USUARIO_SISTEMA] Remove started - usuarioSistemaId={}", usuarioSistemaId);

        UsuarioSistema usuarioSistema = usuarioSistemaRepository.findById(usuarioSistemaId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));
        rbacAuthorizationService.validateCanManageSistema(usuarioSistema.getSistema().getId());

        usuarioSistemaRepository.delete(usuarioSistema);
        log.info("[USUARIO_SISTEMA] Remove success - usuarioSistemaId={}", usuarioSistemaId);
    }
}
