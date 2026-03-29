package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.UsuarioSistema.CriarUsuarioSistemaDTO;
import com.api.auth.Application.DTOs.UsuarioSistema.UsuarioSistemaDTO;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Domain.Entities.Role;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Infra.Repositories.RoleRepository;
import com.api.auth.Infra.Repositories.SistemaRepository;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import com.api.auth.Infra.Repositories.UsuarioSistemaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioSistemaService {

    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final SistemaRepository sistemaRepository;
    private final UsuarioRepository  usuarioRepository;
    private final RoleRepository roleRepository;
    private final MappingProfile mappingProfile;

    public UsuarioSistemaService(UsuarioSistemaRepository usuarioSistemaRepository, SistemaRepository sistemaRepository, MappingProfile mappingProfile, UsuarioRepository usuarioRepository, RoleRepository roleRepository) {
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.sistemaRepository = sistemaRepository;
        this.mappingProfile = mappingProfile;
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
    }

    public UsuarioSistemaDTO criar(CriarUsuarioSistemaDTO dto) {

        Sistema sistema = sistemaRepository.findById(dto.getSistemaId())
                .orElseThrow(() -> new RuntimeException("Sistema não encontrado"));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario não encontrado"));

        Role role = roleRepository.findByIdAndSistemaId(dto.getRoleId(), dto.getSistemaId())
                .orElseThrow(() -> new RuntimeException("Role não encontrada para este sistema"));

        boolean existe = usuarioSistemaRepository
                .existsByUsuarioIdAndSistemaId(dto.getUsuarioId(), dto.getSistemaId());

        if (existe)
            throw new RuntimeException("Usuario já vinculado a este sistema");

        UsuarioSistema usuarioSistema = UsuarioSistema.builder()
                .sistema(sistema)
                .usuario(usuario)
                .role(role)
                .build();

        UsuarioSistema saved = usuarioSistemaRepository.save(usuarioSistema);
        return mappingProfile.toDTO(saved);
    }

    public Page<UsuarioSistemaDTO> listar(UUID usuarioId, UUID sistemaId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<UsuarioSistema> lista;

        if(usuarioId != null && sistemaId != null) // filtrou por usuario e sistema
            lista = usuarioSistemaRepository.findByUsuarioIdAndSistemaId(usuarioId, sistemaId, pageable);

        else if(usuarioId != null) // filtrou por usuario
            lista = usuarioSistemaRepository.findByUsuarioId(usuarioId, pageable);

        else if(sistemaId != null) // filtrou por sistema
            lista = usuarioSistemaRepository.findBySistemaId(sistemaId,  pageable);

        else // sem filtro
            lista = usuarioSistemaRepository.findAll(pageable);

        return lista.map(mappingProfile::toDTO);

    }

    public UsuarioSistemaDTO buscarPorId(UUID id){
        UsuarioSistema usuarioSistema = usuarioSistemaRepository.findById(id).orElse(null);
        return mappingProfile.toDTO(usuarioSistema);
    }

    public UsuarioSistemaDTO mudarRoleUser(UUID usuarioSistemaId, UUID novaRoleId) {

        UsuarioSistema usuarioSistema = usuarioSistemaRepository.findById(usuarioSistemaId)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado"));

        Role novaRole = roleRepository
                .findByIdAndSistemaId(novaRoleId, usuarioSistema.getSistema().getId())
                .orElseThrow(() -> new RuntimeException("Role não encontrada para este sistema"));

        usuarioSistema.setRole(novaRole);
        UsuarioSistema updated = usuarioSistemaRepository.save(usuarioSistema);

        return mappingProfile.toDTO(updated);
    }

    public void remover(UUID usuarioSistemaId) {

        UsuarioSistema usuarioSistema = usuarioSistemaRepository.findById(usuarioSistemaId)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado"));

        usuarioSistemaRepository.delete(usuarioSistema);
    }
}
