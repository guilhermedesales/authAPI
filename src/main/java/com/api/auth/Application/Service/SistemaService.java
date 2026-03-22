package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Permissao.PermissaoDTO;
import com.api.auth.Application.DTOs.Sistema.CriarSistemaDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaDTO;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Infra.Repositories.SistemaRepository;
import com.api.auth.Domain.Entities.Sistema;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
public class SistemaService {

    private final SistemaRepository sistemaRepository;
    private final MappingProfile mappingProfile;

    public SistemaService(SistemaRepository sistemaRepository, MappingProfile mappingProfile) {
        this.sistemaRepository = sistemaRepository;
        this.mappingProfile = mappingProfile;
    }

    public SistemaDTO criar(CriarSistemaDTO dto){

        Sistema sistema = new Sistema(dto.getNome());
        Sistema saved = sistemaRepository.save(sistema);
        return mappingProfile.toDTO(saved);
    }

    public Page<SistemaDTO> listar(int page, int size) {

        PageRequest pageable = PageRequest.of(page, size);

        Page<Sistema> result = sistemaRepository.findAll(pageable);

        return result.map(mappingProfile::toDTO);
    }
}
