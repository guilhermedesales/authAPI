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
import com.api.auth.Application.Service.RBACServices.RbacBootstrapService;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Application.Utils.LogSanitizer;
import com.api.auth.Domain.Entities.*;
import com.api.auth.Domain.Enum.SessionTrustLevel;
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
import org.springframework.dao.DataIntegrityViolationException;
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
    private final LoginRiskService loginRiskService;
    private final RbacBootstrapService rbacBootstrapService;

    @Autowired
    public AuthService(UsuarioRepository usuarioRepository,
                       JwtService jwtService,
                       PasswordEncoder encoder,
                       SistemaRepository sistemaRepository,
                       UsuarioSistemaRepository usuarioSistemaRepository,
                       MappingProfile mappingProfile,
                       VerificationCodeService verificationCodeService,
                       SenhaHistoricoService senhaHistoricoService,
                       TentativasLoginService tentativasLoginService,
                       RefreshTokenService refreshTokenService,
                       UserSessionRepository userSessionRepository,
                       GeoLocationService geoLocationService,
                       LoginRiskService loginRiskService,
                       RbacBootstrapService rbacBootstrapService) {
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
        this.loginRiskService = loginRiskService;
        this.rbacBootstrapService = rbacBootstrapService;
    }

    public RegistrarResponseDTO registrar(RegistrarDTO dto) {
        String maskedEmail = LogSanitizer.maskEmail(dto.getEmail());
        log.info("[AUTH] Register attempt - email={}", maskedEmail);

        Sistema sistema = rbacBootstrapService.resolveSystemOrDefault(dto.getSistemaId());

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
        rbacBootstrapService.ensureGlobalAdminRole();
        ensureUserLinkedToSystemWithDefaultRole(saved, sistema);
        log.info("[AUTH] Register success - userId={} email={}", saved.getId(), maskedEmail);
        return mappingProfile.toDTO(saved);
    }

    @Transactional
    public UUID login(LoginDTO dto){

        String maskedEmail = LogSanitizer.maskEmail(dto.getEmail());
        log.info("[AUTH] Login validation started - email={} sistemaId={}", maskedEmail, dto.getSistemaId());

        Sistema sistema = rbacBootstrapService.resolveSystemOrDefault(dto.getSistemaId());

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO));

        if(usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(LocalDateTime.now())){
            throw new ValidationException("Usuário bloqueado até "+ usuario.getBloqueadoAte());
        }

        else if(usuario.isBloqueado()){
            throw new ValidationException("Usuário falhou em logar muitas vezes, para continuar é necessário alterar a senha");
        }

        if(!encoder.matches(dto.getSenha(), usuario.getSenha())) {
            log.warn("[AUTH] Login validation failed - reason=invalid_credentials email={} sistemaId={}", maskedEmail, sistema.getId());

            String mensagem = tentativasLoginService.registrarTentativaFalha(usuario, sistema);
            throw new ValidationException(mensagem);
        }

        ensureUserLinkedToSystemWithDefaultRole(usuario, sistema);

        log.info("[AUTH] Login validation success - userId={} sistemaId={}", usuario.getId(), sistema.getId());
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);
        return verificationCodeService.generateAndSendChallenge(
                usuario,
                TipoVerificacao.LOGIN,
                null,
                sistema.getId(),
                null,
                null
        );
    }

    @Transactional
    public LoginResponseDTO verifyCodeLogin(VerifyCodeDTO dto, RequestContext context){

        VerificationCode verificationCode = verificationCodeService
                .validateCodeByChallenge(dto.getChallengeId(), dto.getCode(), TipoVerificacao.LOGIN);
        Usuario usuario = verificationCode.getUsuario();

        UUID sistemaId = verificationCode.getSistemaId();
        if (sistemaId == null) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        Sistema sistema = sistemaRepository.findById(sistemaId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));

        UsuarioSistema usuarioSistema = usuarioSistemaRepository
                .findByUsuarioAndSistemaWithRolePermissoes(usuario, sistema)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO_SISTEMA));

        UUID requestedDeviceId = dto.getDeviceId() != null
                ? dto.getDeviceId()
                : (context != null ? context.getDeviceId() : null);

        String resolvedLocation = geoLocationService.getLocation(context != null ? context.getIp() : null);
        LoginRiskService.RiskAssessment riskAssessment = loginRiskService.assess(
                usuario,
                sistema,
                requestedDeviceId,
                context != null ? context.getIp() : null,
                resolvedLocation
        );

        if (riskAssessment.stepUpRequired()) {
            UUID stepUpChallengeId = verificationCodeService.generateAndSendChallenge(
                    usuario,
                    TipoVerificacao.LOGIN_STEP_UP,
                    null,
                    sistema.getId(),
                    null,
                    requestedDeviceId,
                    context != null ? context.getIp() : null,
                    context != null ? context.getUserAgent() : null
            );

            log.warn("[AUTH] Login step-up required - userId={} score={} signals={}",
                    usuario.getId(), riskAssessment.score(), riskAssessment.signals());
            return LoginResponseDTO.stepUpRequired(stepUpChallengeId, riskAssessment.score(), riskAssessment.signals());
        }

        UserSession session = resolveOrCreateSession(usuario, sistema, requestedDeviceId, context, resolvedLocation, riskAssessment);

        String accessToken = jwtService.generateToken(usuarioSistema, session);
        String refreshToken = jwtService.createRefreshToken(usuario, session);

        log.info("[AUTH] Login success - userId={} sessionId={}", usuario.getId(), session.getId());

        return LoginResponseDTO.authenticated(
                accessToken,
                refreshToken,
                session.getDeviceId(),
                session.getTrustLevel(),
                session.getRiskScore(),
                loginRiskService.deserializeSignals(session.getRiskSignals())
        );
    }

    @Transactional
    public LoginResponseDTO verifyStepUpLogin(VerifyCodeDTO dto, RequestContext requestContext) {

        VerificationCode verificationCode = verificationCodeService
                .validateCodeByChallenge(dto.getChallengeId(), dto.getCode(), TipoVerificacao.LOGIN_STEP_UP);
        Usuario usuario = verificationCode.getUsuario();

        UUID sistemaId = verificationCode.getSistemaId();
        if (sistemaId == null) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        Sistema sistema = sistemaRepository.findById(sistemaId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO));

        UsuarioSistema usuarioSistema = usuarioSistemaRepository
                .findByUsuarioAndSistemaWithRolePermissoes(usuario, sistema)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO_SISTEMA));

        RequestContext boundContext = new RequestContext();
        boundContext.setIp(verificationCode.getRequestIp() != null
                ? verificationCode.getRequestIp()
                : requestContext != null ? requestContext.getIp() : null);
        boundContext.setUserAgent(verificationCode.getRequestUserAgent() != null
                ? verificationCode.getRequestUserAgent()
                : requestContext != null ? requestContext.getUserAgent() : null);

        UUID boundDeviceId = verificationCode.getDeviceId() != null
                ? verificationCode.getDeviceId()
                : dto.getDeviceId() != null
                    ? dto.getDeviceId()
                    : requestContext != null ? requestContext.getDeviceId() : null;
        boundContext.setDeviceId(boundDeviceId);

        String resolvedLocation = geoLocationService.getLocation(boundContext.getIp());
        LoginRiskService.RiskAssessment riskAssessment = loginRiskService.assess(
                usuario,
                sistema,
                boundDeviceId,
                boundContext.getIp(),
                resolvedLocation
        );

        if (riskAssessment.trustLevel() == SessionTrustLevel.TRUSTED) {
            riskAssessment = new LoginRiskService.RiskAssessment(
                    riskAssessment.score(),
                    SessionTrustLevel.SUSPICIOUS,
                    false,
                    riskAssessment.signals()
            );
        }

        UserSession session = resolveOrCreateSession(usuario, sistema, boundDeviceId, boundContext, resolvedLocation, riskAssessment);
        String accessToken = jwtService.generateToken(usuarioSistema, session);
        String refreshToken = jwtService.createRefreshToken(usuario, session);

        log.info("[AUTH] Step-up login success - userId={} sessionId={} score={}",
                usuario.getId(), session.getId(), riskAssessment.score());

        return LoginResponseDTO.authenticated(
                accessToken,
                refreshToken,
                session.getDeviceId(),
                session.getTrustLevel(),
                session.getRiskScore(),
                loginRiskService.deserializeSignals(session.getRiskSignals())
        );
    }

    @Transactional
    public UUID alterarSenha(UUID usuarioId, AlterarSenhaDTO dto) {
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
        UUID challengeId = verificationCodeService.generateAndSendChallenge(
                usuario,
                TipoVerificacao.ALTERAR_SENHA,
                novaSenhaHash,
                null,
                dto.getRevogarSessoes(),
                null

        );
        log.info("[AUTH] Password change verification sent - userId={}", usuarioId);
        return challengeId;
    }

    @Transactional
    public void confirmarAlteracaoSenha(UUID challengeId, String code) {
        log.info("[AUTH] Password change verification started");

        VerificationCode verificationCode = verificationCodeService
                .validateCodeByChallenge(challengeId, code, TipoVerificacao.ALTERAR_SENHA);
        Usuario usuario = verificationCode.getUsuario();

        // salva a senha atual no histórico antes de trocar
        senhaHistoricoService.salvarNoHistorico(usuario, usuario.getSenha());

        // aplica a nova senha que estava guardada no código
        usuario.setSenha(verificationCode.getNovaSenhaHash());
        usuarioRepository.save(usuario);

        if (Boolean.TRUE.equals(verificationCode.getRevogarSessoes())) {
            UUID currentSessionId = resolveAuthenticatedSessionId();
            UserSession currentSession = userSessionRepository.findById(currentSessionId)
                    .orElseThrow(() -> new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA));

            if (!currentSession.getUsuario().getId().equals(usuario.getId())) {
                throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
            }

            refreshTokenService.revokeAllByUsuarioExceptOne(usuario, currentSessionId);
        }

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

        validarSenha(dto.getNovaSenha()); // senha forte

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

        UsuarioSistema usuarioSistema = usuarioSistemaRepository.findByUsuarioAndSistemaWithRolePermissoes(usuario, sistema)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO_SISTEMA));

        UUID requestedDeviceId = context != null ? context.getDeviceId() : null;
        String resolvedLocation = geoLocationService.getLocation(context != null ? context.getIp() : null);
        LoginRiskService.RiskAssessment riskAssessment = loginRiskService.assess(
                usuario,
                sistema,
                requestedDeviceId,
                context != null ? context.getIp() : null,
                resolvedLocation
        );

        UserSession session = resolveOrCreateSessionForForgotPassword(
                usuario,
                sistema,
                requestedDeviceId,
                context,
                resolvedLocation,
                riskAssessment
        );
        String accessToken = jwtService.generateToken(usuarioSistema, session);
        String refreshToken = jwtService.createRefreshToken(usuario, session);

        log.info("[AUTH] Forgot-password confirmation success - userId={} sistemaId={}", usuario.getId(), sistema.getId());
        return LoginResponseDTO.authenticated(
                accessToken,
                refreshToken,
                session.getDeviceId(),
                session.getTrustLevel(),
                session.getRiskScore(),
                loginRiskService.deserializeSignals(session.getRiskSignals())
        );
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

    private UUID resolveAuthenticatedSessionId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getDetails() instanceof Claims claims)) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        String sessionIdClaim = claims.get("sessionId", String.class);
        if (sessionIdClaim == null || sessionIdClaim.isBlank()) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        try {
            return UUID.fromString(sessionIdClaim);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }
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

    private UserSession resolveOrCreateSession(Usuario usuario,
                                               Sistema sistema,
                                               UUID deviceId,
                                               RequestContext context,
                                               String resolvedLocation,
                                               LoginRiskService.RiskAssessment riskAssessment) {
        if (deviceId != null) {
            UserSession existingSession = userSessionRepository
                    .findByUsuarioIdAndSistemaIdAndDeviceIdAndRevokedAtIsNull(usuario.getId(), sistema.getId(), deviceId)
                    .orElse(null);

            if (existingSession != null) {
                return updateSessionContext(existingSession, context, resolvedLocation, riskAssessment);
            }
        }

        return createSession(usuario, sistema, context, deviceId, resolvedLocation, riskAssessment);
    }

    private UserSession resolveOrCreateSessionForForgotPassword(Usuario usuario,
                                                                Sistema sistema,
                                                                UUID deviceId,
                                                                RequestContext context,
                                                                String resolvedLocation,
                                                                LoginRiskService.RiskAssessment riskAssessment) {
        if (deviceId != null) {
            UserSession existingActiveSession = userSessionRepository
                    .findByUsuarioIdAndSistemaIdAndDeviceIdAndRevokedAtIsNull(usuario.getId(), sistema.getId(), deviceId)
                    .orElse(null);

            if (existingActiveSession != null) {
                return updateSessionContext(existingActiveSession, context, resolvedLocation, riskAssessment);
            }

            UserSession existingSession = userSessionRepository
                    .findTopByUsuarioIdAndSistemaIdAndDeviceIdOrderByUpdatedAtDesc(usuario.getId(), sistema.getId(), deviceId)
                    .orElse(null);

            if (existingSession != null) {
                existingSession.setRevokedAt(null);
                return updateSessionContext(existingSession, context, resolvedLocation, riskAssessment);
            }
        }

        return createSession(usuario, sistema, context, deviceId, resolvedLocation, riskAssessment);
    }

    private UserSession createSession(Usuario usuario,
                                      Sistema sistema,
                                      RequestContext context,
                                      UUID requestedDeviceId,
                                      String resolvedLocation,
                                      LoginRiskService.RiskAssessment riskAssessment) {
        UserSession session = new UserSession();
        session.setUsuario(usuario);
        session.setSistema(sistema);
        session.setDeviceId(requestedDeviceId != null ? requestedDeviceId : UUID.randomUUID());
        session.setIp(context != null ? context.getIp() : null);
        session.setDeviceName(parseUserAgent(context != null ? context.getUserAgent() : null));
        session.setLocation(resolvedLocation);
        session.setTrustLevel(riskAssessment.trustLevel());
        session.setRiskScore(riskAssessment.score());
        session.setRiskSignals(loginRiskService.serializeSignals(riskAssessment.signals()));
        session.setLastUsedAt(Instant.now());
        return userSessionRepository.save(session);
    }

    private UserSession updateSessionContext(UserSession session,
                                             RequestContext context,
                                             String resolvedLocation,
                                             LoginRiskService.RiskAssessment riskAssessment) {
        session.setIp(context != null ? context.getIp() : session.getIp());
        session.setDeviceName(parseUserAgent(context != null ? context.getUserAgent() : null));
        session.setLocation(resolvedLocation);
        session.setTrustLevel(riskAssessment.trustLevel());
        session.setRiskScore(riskAssessment.score());
        session.setRiskSignals(loginRiskService.serializeSignals(riskAssessment.signals()));
        session.setLastUsedAt(Instant.now());
        return userSessionRepository.save(session);
    }

    private UsuarioSistema ensureUserLinkedToSystemWithDefaultRole(Usuario usuario, Sistema sistema) {
        return usuarioSistemaRepository.findByUsuarioAndSistema(usuario, sistema)
                .orElseGet(() -> {
                    rbacBootstrapService.ensureGlobalAdminRole();
                    Role userRole = rbacBootstrapService.getOrCreateUserRole(sistema);

                    UsuarioSistema novoVinculo = UsuarioSistema.builder()
                            .usuario(usuario)
                            .sistema(sistema)
                            .role(userRole)
                            .build();

                    try {
                        log.info("[AUTH] Auto-link user to system with default USER role - userId={} sistemaId={}",
                                usuario.getId(), sistema.getId());
                        return usuarioSistemaRepository.save(novoVinculo);
                    } catch (DataIntegrityViolationException ex) {
                        return usuarioSistemaRepository.findByUsuarioAndSistema(usuario, sistema)
                                .orElseThrow(() -> ex);
                    }
                });
    }
}
