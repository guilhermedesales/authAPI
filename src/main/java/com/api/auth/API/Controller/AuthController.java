package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Auth.AlterarSenhaDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaConfirmDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaVerifyCodeDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaVerifyResponseDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginResponseDTO;
import com.api.auth.Application.DTOs.Auth.RefreshToken.RefreshTokenDTO;
import com.api.auth.Application.DTOs.Auth.RefreshToken.RefreshTokenResponseDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarResponseDTO;
import com.api.auth.Application.DTOs.Auth.VerifyCodeDTO;
import com.api.auth.Application.DTOs.RequestContext;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Service.AuthService;
import com.api.auth.Application.Service.JwtService;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Application.Utils.LogSanitizer;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Infra.Repositories.UsuarioSistemaRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UsuarioSistemaRepository usuarioSistemaRepository;

    public AuthController(AuthService authService, JwtService jwtService, UsuarioSistemaRepository usuarioSistemaRepository) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
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
    public ResponseEntity<LoginResponseDTO> verifyCodeLogin(
            @RequestBody @Valid VerifyCodeDTO dto,
            HttpServletRequest request
    ) {

        return ResponseEntity.ok(authService.verifyCodeLogin(dto, buildRequestContext(request)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(@RequestBody RefreshTokenDTO dto) {
        log.info("[AUTH] Refresh token request received");
        String requestToken = dto.getRefreshToken();

        // Service performs reuse detection + rotation in a single transaction.
        JwtService.RefreshRotationResult rotationResult = jwtService.rotateRefreshToken(requestToken);
        Usuario usuario = rotationResult.usuario();

        UsuarioSistema usuarioSistema = usuarioSistemaRepository
                .findByUsuario(usuario)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));

        String accessToken = jwtService.generateToken(usuarioSistema);
        log.info("[AUTH] Refresh token success - userId={}", usuario.getId());
        return ResponseEntity.ok(new RefreshTokenResponseDTO(accessToken, rotationResult.refreshToken()));
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

    @PostMapping("/esqueci-senha/request")
    public ResponseEntity<Void> esqueciSenhaRequest(@RequestBody @Valid EsqueciSenhaDTO dto) {
        log.info("[AUTH] Forgot-password request endpoint hit - email={}", LogSanitizer.maskEmail(dto.getEmail()));
        authService.esqueciSenha(dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/esqueci-senha/verify-code")
    public ResponseEntity<EsqueciSenhaVerifyResponseDTO> esqueciSenhaVerifyCode(@RequestBody @Valid EsqueciSenhaVerifyCodeDTO dto) {
        UUID challengeId = authService.verificarCodigoEsqueciSenha(dto);
        return ResponseEntity.ok(new EsqueciSenhaVerifyResponseDTO(challengeId));
    }

    @PostMapping("/esqueci-senha/confirm")
    public ResponseEntity<LoginResponseDTO> esqueciSenhaConfirm(@RequestBody @Valid EsqueciSenhaConfirmDTO dto,
                                                                HttpServletRequest request) {
        LoginResponseDTO response = authService.confirmarEsqueciSenha(dto, buildRequestContext(request));
        return ResponseEntity.ok(response);
    }

    private RequestContext buildRequestContext(HttpServletRequest request) {
        RequestContext context = new RequestContext();
        context.setIp(extractClientIp(request));
        context.setUserAgent(request.getHeader("User-Agent"));
        return context;
    }

    private String extractClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "CF-Connecting-IP", // Cloudflare
                "True-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank()) {
                // pega o primeiro IP se tiver vários
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return normalizeIp(ip);
            }
        }

        return normalizeIp(request.getRemoteAddr());
    }

    private String normalizeIp(String ip) {
        if (ip == null) return null;

        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return "127.0.0.1";
        }

        // remove prefixo IPv6 tipo ::ffff:127.0.0.1
        if (ip.startsWith("::ffff:")) {
            return ip.substring(7);
        }

        return ip;
    }
}
