package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Auth.LoginDTO;
import com.api.auth.Application.DTOs.Auth.RegistrarDTO;
import com.api.auth.Application.DTOs.Usuario.CriarUsuarioDTO;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Infra.Repositories.SistemaRepository;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import com.api.auth.Infra.Repositories.UsuarioSistemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final SistemaRepository sistemaRepository;

    @Autowired
    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService, PasswordEncoder encoder, SistemaRepository sistemaRepository, UsuarioSistemaRepository usuarioSistemaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.sistemaRepository = sistemaRepository;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.encoder = encoder;
    }

    public Usuario registrar(RegistrarDTO dto) {

        Usuario usuario = new Usuario();

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(encoder.encode(dto.getSenha()));

        return usuarioRepository.save(usuario);
    }

    public String login(LoginDTO dto){
        Sistema sistema = sistemaRepository.findById(dto.getSistemaId())
                .orElseThrow(() -> new RuntimeException("Sistema não encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UsuarioSistema usuarioSistema = usuarioSistemaRepository.findByUsuarioAndSistema(usuario, sistema)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para esse sistema"));

        if(!encoder.matches(dto.getSenha(), usuario.getSenha()))
            throw new RuntimeException("Senha inválida");

        return jwtService.generateToken(usuarioSistema);
    }

}
