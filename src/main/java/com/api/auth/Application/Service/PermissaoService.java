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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.PERMISSAO_NAO_ENCONTRADO));

        Permissao permissao = Permissao.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .build();

        Permissao saved = permissaoRepository.save(permissao);
        return mappingProfile.toDTO(saved);
    }

    public Page<PermissaoDTO> listar(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Permissao> permissao = permissaoRepository.findAll(pageable);
        return permissao.map(mappingProfile::toDTO);
    }

    public PermissaoDTO editar(UUID id, CriarPermissaoDTO dto) {

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
        return mappingProfile.toDTO(saved);
    }

}
