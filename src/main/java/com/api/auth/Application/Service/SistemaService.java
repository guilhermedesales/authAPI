package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;
import com.api.auth.Application.DTOs.Sistema.CriarSistemaDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaListDTO;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Infra.Repositories.SistemaRepository;
import com.api.auth.Domain.Entities.Sistema;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

@Service
public class SistemaService {

    private final SistemaRepository sistemaRepository;
    private final MappingProfile mappingProfile;

    public SistemaService(SistemaRepository sistemaRepository, MappingProfile mappingProfile) {
        this.sistemaRepository = sistemaRepository;
        this.mappingProfile = mappingProfile;
    }

    public SistemaListDTO criar(CriarSistemaDTO dto){

        Sistema sistema = Sistema.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .build();
        Sistema saved = sistemaRepository.save(sistema);
        return mappingProfile.toListDTO(saved);
    }

    public Page<SistemaListDTO> listar(int page, int size) {

        PageRequest pageable = PageRequest.of(page, size);

        Page<Sistema> result = sistemaRepository.findAll(pageable);

        return result.map(mappingProfile::toListDTO);
    }

    public SistemaDTO buscarPorId(UUID id) {
        Sistema sistema = sistemaRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));
        return mappingProfile.toDTO(sistema);
    }
}
