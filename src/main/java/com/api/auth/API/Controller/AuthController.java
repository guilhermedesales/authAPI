package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Auth.Login.LoginDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginResponseDTO;
import com.api.auth.Application.DTOs.Auth.RefreshToken.RefreshTokenDTO;
import com.api.auth.Application.DTOs.Auth.RefreshToken.RefreshTokenResponseDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarResponseDTO;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Service.AuthService;
import com.api.auth.Application.Service.JwtService;
import com.api.auth.Application.Service.UsuarioService;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Domain.Entities.RefreshToken;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Infra.Repositories.UsuarioSistemaRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;
    private final JwtService jwtService;
    private final UsuarioSistemaRepository usuarioSistemaRepository;

    public AuthController(UsuarioService usuarioService, AuthService authService, JwtService jwtService, UsuarioSistemaRepository usuarioSistemaRepository) {
        this.usuarioService = usuarioService;
        this.authService = authService;
        this.jwtService = jwtService;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
    }

    @PostMapping("/register")
    public RegistrarResponseDTO registrar(@Valid @RequestBody RegistrarDTO dto) {
        return authService.registrar(dto);
    }

    @PostMapping("login")
    public LoginResponseDTO login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDTO dto) {
        String requestToken = dto.getRefreshToken();

        return jwtService.findByToken(requestToken)
                .map(jwtService::verifyExpiration)
                .map(RefreshToken::getUsuario)
                .map(usuario -> {

                    UsuarioSistema usuarioSistema = usuarioSistemaRepository
                            .findByUsuario(usuario)
                            .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));

                    // gerar novo JWT
                    String accessToken = jwtService.generateToken(usuarioSistema);

                    // gerar refresh token rotativo
                    RefreshToken newRefresh = jwtService.createRefreshToken(usuario);

                    return ResponseEntity.ok(new RefreshTokenResponseDTO(accessToken, newRefresh.getToken()));
                })
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.REFRESH_TOKEN_NAO_ENCONTRADO));
    }
}
