package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Auth.Login.LoginDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginResponseDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarResponseDTO;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Exceptions.ValidationException;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.RefreshToken;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Infra.Repositories.SistemaRepository;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import com.api.auth.Infra.Repositories.UsuarioSistemaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final SistemaRepository sistemaRepository;
    private final MappingProfile mappingProfile;

    @Autowired
    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService, PasswordEncoder encoder, SistemaRepository sistemaRepository, UsuarioSistemaRepository usuarioSistemaRepository, MappingProfile mappingProfile) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.sistemaRepository = sistemaRepository;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.encoder = encoder;
        this.mappingProfile = mappingProfile;
    }

    public RegistrarResponseDTO registrar(RegistrarDTO dto) {

        validarSenha(dto.getSenha());
        if (usuarioRepository.existsByEmail(dto.getEmail()))
            throw new ValidationException(ErrorMessages.Auth.EMAIL_JA_CADASTRADO);

        Usuario usuario = new Usuario();

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(encoder.encode(dto.getSenha()));

        Usuario saved = usuarioRepository.save(usuario);

        return mappingProfile.toDTO(saved);
    }

    @Transactional
    public LoginResponseDTO login(LoginDTO dto){
        Sistema sistema = sistemaRepository.findById(dto.getSistemaId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO));

        UsuarioSistema usuarioSistema = usuarioSistemaRepository.findByUsuarioAndSistema(usuario, sistema)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO_SISTEMA));

        if(!encoder.matches(dto.getSenha(), usuario.getSenha()))
            throw new NotFoundException(ErrorMessages.Auth.CREDENCIAIS_INVALIDAS);

        // gera o access token com todas as claims
        String token = jwtService.generateToken(usuarioSistema);

        // cria o refresh token e salva no banco
        RefreshToken refreshToken = jwtService.createRefreshToken(usuario);

        // retorna ambos
        return new LoginResponseDTO(token, refreshToken.getToken());
    }

    /////// utilitários /////////

    private void validarSenha(String senha){
        List<String> erros = new ArrayList<>();

        if (senha.length() < 8)
            erros.add(ErrorMessages.Senha.MINIMO_CARACTERES);

        if (!senha.matches(".*[A-Z].*"))
            erros.add(ErrorMessages.Senha.LETRA_MAIUSCULA);

        if (!senha.matches(".*[a-z].*"))
            erros.add(ErrorMessages.Senha.LETRA_MINUSCULA);

        if (!senha.matches(".*\\d.*"))
            erros.add(ErrorMessages.Senha.NUMERO);

        if (!senha.matches(".*[@$!%*?&].*"))
            erros.add(ErrorMessages.Senha.CARACTERE_ESPECIAL);

        if (!erros.isEmpty())
            throw new ValidationException(erros);
    }

}
