package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Auth.AlterarSenhaDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaConfirmDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaVerifyCodeDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginResponseDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarResponseDTO;
import com.api.auth.Application.DTOs.Auth.VerifyCodeDTO;
import com.api.auth.Application.DTOs.Auth.RequestContext;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Exceptions.ValidationException;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Application.Utils.LogSanitizer;
import com.api.auth.Domain.Entities.*;
import com.api.auth.Domain.Enum.TipoVerificacao;
import com.api.auth.Infra.Repositories.SistemaRepository;
import com.api.auth.Infra.Repositories.UserSessionRepository;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import com.api.auth.Infra.Repositories.UsuarioSistemaRepository;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final SistemaRepository sistemaRepository;
    private final MappingProfile mappingProfile;
    private final VerificationCodeService verificationCodeService;
    private final SenhaHistoricoService senhaHistoricoService;
    private final TentativasLoginService tentativasLoginService;
    private final RefreshTokenService refreshTokenService;
    private final UserSessionRepository userSessionRepository;
    private final GeoLocationService geoLocationService;

    @Autowired
    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService, PasswordEncoder encoder, SistemaRepository sistemaRepository, UsuarioSistemaRepository usuarioSistemaRepository, MappingProfile mappingProfile, VerificationCodeService verificationCodeService, SenhaHistoricoService senhaHistoricoService, TentativasLoginService tentativasLoginService, RefreshTokenService refreshTokenService, UserSessionRepository userSessionRepository, GeoLocationService geoLocationService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.sistemaRepository = sistemaRepository;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.encoder = encoder;
        this.mappingProfile = mappingProfile;
        this.verificationCodeService = verificationCodeService;
        this.senhaHistoricoService = senhaHistoricoService;
        this.tentativasLoginService = tentativasLoginService;
        this.refreshTokenService = refreshTokenService;
        this.userSessionRepository = userSessionRepository;
        this.geoLocationService = geoLocationService;
    }

    public RegistrarResponseDTO registrar(RegistrarDTO dto) {
        String maskedEmail = LogSanitizer.maskEmail(dto.getEmail());
        log.info("[AUTH] Register attempt - email={}", maskedEmail);

        validarSenha(dto.getSenha());
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            log.warn("[AUTH] Register failed - reason=email_already_registered email={}", maskedEmail);
            throw new ValidationException(ErrorMessages.Auth.EMAIL_JA_CADASTRADO);
        }

        Usuario usuario = Usuario.builder()
                .email(dto.getEmail())
                .nome(dto.getNome())
                .senha(encoder.encode(dto.getSenha()))
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        log.info("[AUTH] Register success - userId={} email={}", saved.getId(), maskedEmail);
        return mappingProfile.toDTO(saved);
    }

    @Transactional
    public void login(LoginDTO dto){

        String maskedEmail = LogSanitizer.maskEmail(dto.getEmail());
        log.info("[AUTH] Login validation started - email={} sistemaId={}", maskedEmail, dto.getSistemaId());

        Sistema sistema = sistemaRepository.findById(dto.getSistemaId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO));

        if(usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(LocalDateTime.now())){
            throw new ValidationException("Usuário bloqueado até "+ usuario.getBloqueadoAte());
        }

        else if(usuario.isBloqueado()){
            throw new ValidationException("Usuário falhou em logar muitas vezes, para continuar é necessário alterar a senha");
        }

        usuarioSistemaRepository.findByUsuarioAndSistema(usuario, sistema)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO_SISTEMA));

        if(!encoder.matches(dto.getSenha(), usuario.getSenha())) {
            log.warn("[AUTH] Login validation failed - reason=invalid_credentials email={} sistemaId={}", maskedEmail, sistema.getId());

            String mensagem = tentativasLoginService.registrarTentativaFalha(usuario, sistema);
            throw new ValidationException(mensagem);
        }

        log.info("[AUTH] Login validation success - userId={} sistemaId={}", usuario.getId(), sistema.getId());
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);
        verificationCodeService.generateAndSend(usuario, TipoVerificacao.LOGIN);
    }

    @Transactional
    public LoginResponseDTO verifyCodeLogin(VerifyCodeDTO dto, RequestContext context){

        VerificationCode verificationCode = verificationCodeService.validateCode(dto.getCode(), TipoVerificacao.LOGIN);
        Usuario usuario = verificationCode.getUsuario();

        UsuarioSistema usuarioSistema = usuarioSistemaRepository
                .findByUsuario(usuario)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));

        UUID requestedDeviceId = dto.getDeviceId() != null
                ? dto.getDeviceId()
                : (context != null ? context.getDeviceId() : null);

        UserSession session = resolveOrCreateSession(usuario, requestedDeviceId, context);

        String accessToken = jwtService.generateToken(usuarioSistema, session);
        String refreshToken = jwtService.createRefreshToken(usuario, session);

        log.info("[AUTH] Login success - userId={} sessionId={}", usuario.getId(), session.getId());

        return new LoginResponseDTO(accessToken, refreshToken, session.getDeviceId());
    }

    @Transactional
    public void alterarSenha(UUID usuarioId, AlterarSenhaDTO dto) {
        log.info("[AUTH] Password change requested - userId={}", usuarioId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO));

        validarSenha(dto.getNovaSenha());

        // valida a senha atual
        if (!encoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
            log.warn("[AUTH] Password change failed - reason=invalid_current_password userId={}", usuarioId);
            throw new ValidationException("Senha atual incorreta.");
        }

        // valida se a nova senha já foi usada antes
        senhaHistoricoService.validarSenhaNaoReutilizada(usuario, dto.getNovaSenha());

        log.info("[AUTH] Password policy validated - userId={}", usuarioId);
        String novaSenhaHash = encoder.encode(dto.getNovaSenha());
        verificationCodeService.generateAndSend(usuario, TipoVerificacao.ALTERAR_SENHA, novaSenhaHash);
        log.info("[AUTH] Password change verification sent - userId={}", usuarioId);
    }

    @Transactional
    public void confirmarAlteracaoSenha(String code) {
        log.info("[AUTH] Password change verification started");

        VerificationCode verificationCode = verificationCodeService.validateCode(code, TipoVerificacao.ALTERAR_SENHA);
        Usuario usuario = verificationCode.getUsuario();

        // salva a senha atual no histórico antes de trocar
        senhaHistoricoService.salvarNoHistorico(usuario, usuario.getSenha());

        // aplica a nova senha que estava guardada no código
        usuario.setSenha(verificationCode.getNovaSenhaHash());
        usuarioRepository.save(usuario);

        refreshTokenService.revokeAllByUsuario(usuario);

        log.info("[AUTH] Password change completed - userId={}", usuario.getId());
    }

    @Transactional
    public void esqueciSenha(EsqueciSenhaDTO dto){
        String maskedEmail = LogSanitizer.maskEmail(dto.getEmail());
        log.info("[AUTH] Forgot-password request received - email={}", maskedEmail);

        // Resposta é sempre "sucesso" para evitar enumeração de emails válidos
        usuarioRepository.findByEmail(dto.getEmail()).ifPresentOrElse(
                usuario -> {
                    verificationCodeService.generateAndSend(usuario, TipoVerificacao.ESQUECI_SENHA, null);
                    log.info("[AUTH] Forgot-password code sent - userId={} email={}", usuario.getId(), maskedEmail);
                },
                () -> log.info("[AUTH] Forgot-password request processed for unknown email - email={}", maskedEmail)
        );
    }

    @Transactional
    public UUID verificarCodigoEsqueciSenha(EsqueciSenhaVerifyCodeDTO dto) {
        log.info("[AUTH] Forgot-password code verification started - email={}", LogSanitizer.maskEmail(dto.getEmail()));
        UUID challengeId = verificationCodeService.validateForgotPasswordCode(dto.getEmail(), dto.getCode());
        log.info("[AUTH] Forgot-password code verification success - challengeId={}", challengeId);
        return challengeId;
    }

    @Transactional
    public LoginResponseDTO confirmarEsqueciSenha(EsqueciSenhaConfirmDTO dto, RequestContext context) {
        log.info("[AUTH] Forgot-password confirmation started - challengeId={}", dto.getChallengeId());

        if (!dto.getNovaSenha().equals(dto.getConfirmarNovaSenha())) {
            throw new ValidationException("Nova senha e confirmação não conferem.");
        }

        validarSenha(dto.getNovaSenha());

        VerificationCode verificationCode = verificationCodeService.validateForgotPasswordChallenge(dto.getChallengeId());
        Usuario usuario = verificationCode.getUsuario();

        // Impede reutilização de senhas antigas tambem no fluxo de recuperação
        senhaHistoricoService.validarSenhaNaoReutilizada(usuario, dto.getNovaSenha());
        senhaHistoricoService.salvarNoHistorico(usuario, usuario.getSenha());

        usuario.setSenha(encoder.encode(dto.getNovaSenha()));
        usuario.setBloqueado(false);
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);

        // Revoga sessões antigas antes de emitir novos tokens
        refreshTokenService.revokeAllByUsuario(usuario);

        Sistema sistema = sistemaRepository.findById(dto.getSistemaId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));

        UsuarioSistema usuarioSistema = usuarioSistemaRepository.findByUsuarioAndSistema(usuario, sistema)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO_SISTEMA));

        UserSession session = createSession(usuario, context, context != null ? context.getDeviceId() : null);
        String accessToken = jwtService.generateToken(usuarioSistema, session);
        String refreshToken = jwtService.createRefreshToken(usuario, session);

        log.info("[AUTH] Forgot-password confirmation success - userId={} sistemaId={}", usuario.getId(), sistema.getId());
        return new LoginResponseDTO(accessToken, refreshToken, session.getDeviceId());
    }

    @Transactional
    public void logout(String authorizationHeader) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usuarioId = (String) authentication.getPrincipal();
        log.info("[AUTH] Logout requested - userId={}", usuarioId);

        String token = extractBearerToken(authorizationHeader);
        Claims claims = jwtService.extractClaims(token);
        String sessionIdClaim = claims.get("sessionId", String.class);

        if (sessionIdClaim == null || sessionIdClaim.isBlank()) {
            Usuario usuario = usuarioRepository.findById(UUID.fromString(usuarioId))
                    .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO));
            refreshTokenService.revokeAllByUsuario(usuario);
            log.warn("[AUTH] Logout fallback for legacy token - userId={} action=revoke_all", usuarioId);
            return;
        }

        UUID sessionId = UUID.fromString(sessionIdClaim);
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA));

        if (!session.getUsuario().getId().toString().equals(usuarioId)) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        refreshTokenService.revokeBySession(session);
        log.info("[AUTH] Logout success - session revoked - userId={} sessionId={}", usuarioId, sessionId);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }
        return authorizationHeader.substring(7);
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

    private String parseUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }

        UserAgent agent = UserAgent.parseUserAgentString(userAgent);

        String browser = agent.getBrowser().getName();
        String os = agent.getOperatingSystem().getName();

        return browser + " (" + os + ")";
    }

    private UserSession resolveOrCreateSession(Usuario usuario, UUID deviceId, RequestContext context) {
        if (deviceId != null) {
            UserSession existingSession = userSessionRepository
                    .findByUsuarioIdAndDeviceIdAndRevokedAtIsNull(usuario.getId(), deviceId)
                    .orElse(null);

            if (existingSession != null) {
                return updateSessionContext(existingSession, context);
            }
        }

        return createSession(usuario, context, deviceId);
    }

    private UserSession createSession(Usuario usuario, RequestContext context, UUID requestedDeviceId) {
        UserSession session = new UserSession();
        session.setUsuario(usuario);
        session.setDeviceId(requestedDeviceId != null ? requestedDeviceId : UUID.randomUUID());
        session.setIp(context != null ? context.getIp() : null);
        session.setDeviceName(parseUserAgent(context != null ? context.getUserAgent() : null));
        session.setLocation(geoLocationService.getLocation(context != null ? context.getIp() : null));
        session.setLastUsedAt(Instant.now());
        return userSessionRepository.save(session);
    }

    private UserSession updateSessionContext(UserSession session, RequestContext context) {
        session.setIp(context != null ? context.getIp() : session.getIp());
        session.setDeviceName(parseUserAgent(context != null ? context.getUserAgent() : null));
        session.setLocation(geoLocationService.getLocation(context != null ? context.getIp() : null));
        session.setLastUsedAt(Instant.now());
        return userSessionRepository.save(session);
    }
}
