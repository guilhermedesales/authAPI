package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Permissao.CriarPermissaoDTO;
import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Infra.Repositories.PermissaoRepository;
import com.api.auth.Infra.Repositories.RoleRepository;
import com.api.auth.Domain.Entities.Permissao;
import com.api.auth.Domain.Entities.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PermissaoService {

    private final PermissaoRepository permissaoRepository;
    private final RoleRepository roleRepository;
    private final MappingProfile mappingProfile;

    public PermissaoService(PermissaoRepository permissaoRepository, RoleRepository roleRepository, MappingProfile mappingProfile) {
        this.permissaoRepository = permissaoRepository;
        this.roleRepository = roleRepository;
        this.mappingProfile = mappingProfile;
    }

    public PermissaoDTO criar(CriarPermissaoDTO dto) {
        log.info("[PERMISSAO] Create started - roleId={} nome={}", dto.getRoleId(), dto.getNome());
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.PERMISSAO_NAO_ENCONTRADO));

        Permissao permissao = Permissao.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .build();

        Permissao saved = permissaoRepository.save(permissao);
        log.info("[PERMISSAO] Create success - permissaoId={} roleId={}", saved.getId(), role.getId());
        return mappingProfile.toDTO(saved);
    }

    public Page<PermissaoDTO> listar(int page, int size){
        log.debug("[PERMISSAO] List started - page={} size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Permissao> permissao = permissaoRepository.findAll(pageable);
        log.debug("[PERMISSAO] List success - totalElements={}", permissao.getTotalElements());
        return permissao.map(mappingProfile::toDTO);
    }

    public PermissaoDTO editar(UUID id, CriarPermissaoDTO dto) {
        log.info("[PERMISSAO] Update started - permissaoId={} roleId={}", id, dto.getRoleId());

        Permissao permissao = new Permissao();
        permissaoRepository.findById(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessages.Recursos.PERMISSAO_NAO_ENCONTRADO));
        permissao.setNome(dto.getNome());

        if(dto.getRoleId() != null){
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.ROLE_NAO_ENCONTRADA));
            permissao.setRole(role);
        }
        Permissao saved = permissaoRepository.save(permissao);
        log.info("[PERMISSAO] Update success - permissaoId={}", saved.getId());
        return mappingProfile.toDTO(saved);
    }

}
