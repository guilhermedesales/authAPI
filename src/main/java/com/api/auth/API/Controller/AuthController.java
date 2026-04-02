package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Auth.AlterarSenhaDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginResponseDTO;
import com.api.auth.Application.DTOs.Auth.RefreshToken.RefreshTokenDTO;
import com.api.auth.Application.DTOs.Auth.RefreshToken.RefreshTokenResponseDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarResponseDTO;
import com.api.auth.Application.DTOs.Auth.VerifyCodeDTO;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Service.AuthService;
import com.api.auth.Application.Service.JwtService;
import com.api.auth.Application.Service.UsuarioService;
import com.api.auth.Application.Service.VerificationCodeService;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Application.Utils.LogSanitizer;
import com.api.auth.Domain.Entities.RefreshToken;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Domain.Entities.VerificationCode;
import com.api.auth.Domain.Enum.TipoVerificacao;
import com.api.auth.Infra.Repositories.UsuarioSistemaRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;
    private final JwtService jwtService;
    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final VerificationCodeService verificationCodeService;

    public AuthController(UsuarioService usuarioService, AuthService authService, JwtService jwtService, UsuarioSistemaRepository usuarioSistemaRepository, VerificationCodeService verificationCodeService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
        this.jwtService = jwtService;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.verificationCodeService = verificationCodeService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrarResponseDTO> registrar(@Valid @RequestBody RegistrarDTO dto) {
        log.info("[AUTH] Register request - email={}", LogSanitizer.maskEmail(dto.getEmail()));
        RegistrarResponseDTO response = authService.registrar(dto);
        log.info("[AUTH] Register success - userId={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginDTO dto) {
        log.info("[AUTH] Login attempt - email={} sistemaId={}", LogSanitizer.maskEmail(dto.getEmail()), dto.getSistemaId());
        authService.login(dto);
        log.info("[AUTH] Login challenge sent - email={}", LogSanitizer.maskEmail(dto.getEmail()));
        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping("/login/verify-code")
    public ResponseEntity<LoginResponseDTO> verifyCodeLogin(@RequestBody @Valid VerifyCodeDTO dto) {
        log.info("[AUTH] Login code verification - codeLength={}", dto.getCode() == null ? 0 : dto.getCode().length());
        VerificationCode verificationCode = verificationCodeService.validateCode(dto.getCode(), TipoVerificacao.LOGIN);
        Usuario usuario = verificationCode.getUsuario();

        UsuarioSistema usuarioSistema = usuarioSistemaRepository
                .findByUsuario(usuario)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));

        String accessToken = jwtService.generateToken(usuarioSistema);
        RefreshToken refreshToken = jwtService.createRefreshToken(usuario);

        log.info("[AUTH] Login success - userId={} sistemaId={}", usuario.getId(), usuarioSistema.getSistema().getId());
        return ResponseEntity.ok(new LoginResponseDTO(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDTO dto) {
        log.info("[AUTH] Refresh token request");
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

                    log.info("[AUTH] Refresh token success - userId={}", usuario.getId());
                    return ResponseEntity.ok(new RefreshTokenResponseDTO(accessToken, newRefresh.getToken()));
                })
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.REFRESH_TOKEN_NAO_ENCONTRADO));
    }

    @PutMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(@RequestBody @Valid AlterarSenhaDTO dto,
                                             @AuthenticationPrincipal String usuarioId) {
        log.info("[AUTH] Password change request - userId={}", usuarioId);
        authService.alterarSenha(UUID.fromString(usuarioId), dto);
        log.info("[AUTH] Password change challenge sent - userId={}", usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/alterar-senha/verify-code")
    public ResponseEntity<Void> verifyCodeAlterarSenha(@RequestBody @Valid VerifyCodeDTO dto) {
        log.info("[AUTH] Password change code verification - codeLength={}", dto.getCode() == null ? 0 : dto.getCode().length());
        authService.confirmarAlteracaoSenha(dto.getCode());
        log.info("[AUTH] Password changed successfully via verification code");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }
}
