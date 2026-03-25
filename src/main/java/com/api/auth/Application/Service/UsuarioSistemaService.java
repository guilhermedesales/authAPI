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
import org.springframework.stereotype.Service;

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

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role não encontrada"));

        UsuarioSistema usuarioSistema = new UsuarioSistema(sistema, usuario, role);

        UsuarioSistema saved = usuarioSistemaRepository.save(usuarioSistema);
        return mappingProfile.toDTO(saved);
    }

}
