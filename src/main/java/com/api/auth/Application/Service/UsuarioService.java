package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Usuario.CriarUsuarioDTO;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import com.api.auth.Domain.Entities.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario criar(CriarUsuarioDTO dto) {

        Usuario usuario = new Usuario();

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar() {

        return usuarioRepository.findAll();
    }

}
