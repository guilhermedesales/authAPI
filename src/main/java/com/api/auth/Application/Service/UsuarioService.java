package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Usuario.CriarUsuarioDTO;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import com.api.auth.Domain.Entities.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listar() {

        return usuarioRepository.findAll();
    }

}
