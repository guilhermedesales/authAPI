package com.api.auth.Application.Service;

import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaConfirmDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginResponseDTO;
import com.api.auth.Application.DTOs.Auth.RequestContext;
import com.api.auth.Application.Mapper.MappingProfile;
import com.api.auth.Application.Service.RBACServices.RbacBootstrapService;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.UserSession;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Domain.Enum.SessionTrustLevel;
import com.api.auth.Domain.Enum.TipoVerificacao;
import com.api.auth.Infra.Repositories.SistemaRepository;
import com.api.auth.Infra.Repositories.UserSessionRepository;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import com.api.auth.Infra.Repositories.UsuarioSistemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioSistemaRepository usuarioSistemaRepository;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private SistemaRepository sistemaRepository;
    @Mock
    private MappingProfile mappingProfile;
    @Mock
    private VerificationCodeService verificationCodeService;
    @Mock
    private SenhaHistoricoService senhaHistoricoService;
    @Mock
    private TentativasLoginService tentativasLoginService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private UserSessionRepository userSessionRepository;
    @Mock
    private GeoLocationService geoLocationService;
    @Mock
    private LoginRiskService loginRiskService;
    @Mock
    private RbacBootstrapService rbacBootstrapService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                usuarioRepository,
                jwtService,
                encoder,
                sistemaRepository,
                usuarioSistemaRepository,
                mappingProfile,
                verificationCodeService,
                senhaHistoricoService,
                tentativasLoginService,
                refreshTokenService,
                userSessionRepository,
                geoLocationService,
                loginRiskService,
                rbacBootstrapService
        );
    }

    @Test
    void shouldReuseRevokedDeviceSessionOnForgotPasswordConfirm() {
        UUID userId = UUID.randomUUID();
        UUID sistemaId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        UUID challengeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setId(userId);
        usuario.setSenha("oldHash");

        Sistema sistema = new Sistema();
        sistema.setId(sistemaId);

        UsuarioSistema usuarioSistema = UsuarioSistema.builder()
                .usuario(usuario)
                .sistema(sistema)
                .build();

        UserSession revokedSession = new UserSession();
        revokedSession.setId(sessionId);
        revokedSession.setUsuario(usuario);
        revokedSession.setSistema(sistema);
        revokedSession.setDeviceId(deviceId);
        revokedSession.setRevokedAt(Instant.now());

        com.api.auth.Domain.Entities.VerificationCode verificationCode = new com.api.auth.Domain.Entities.VerificationCode();
        verificationCode.setUsuario(usuario);
        verificationCode.setTipo(TipoVerificacao.ESQUECI_SENHA);

        EsqueciSenhaConfirmDTO dto = new EsqueciSenhaConfirmDTO(
                challengeId,
                "NovaSenha@123",
                "NovaSenha@123",
                sistemaId,
                deviceId
        );

        RequestContext context = new RequestContext();
        context.setDeviceId(deviceId);
        context.setIp("203.0.113.8");
        context.setUserAgent("Mozilla/5.0");

        LoginRiskService.RiskAssessment riskAssessment = new LoginRiskService.RiskAssessment(
                10,
                SessionTrustLevel.TRUSTED,
                false,
                List.of("KNOWN_DEVICE")
        );

        when(verificationCodeService.validateForgotPasswordChallenge(challengeId)).thenReturn(verificationCode);
        when(encoder.encode("NovaSenha@123")).thenReturn("newHash");
        when(sistemaRepository.findById(sistemaId)).thenReturn(Optional.of(sistema));
        when(usuarioSistemaRepository.findByUsuarioAndSistema(usuario, sistema)).thenReturn(Optional.of(usuarioSistema));
        when(geoLocationService.getLocation("203.0.113.8")).thenReturn("Sao Paulo, SP, BR");
        when(loginRiskService.assess(usuario, sistema, deviceId, "203.0.113.8", "Sao Paulo, SP, BR"))
                .thenReturn(riskAssessment);
        when(loginRiskService.serializeSignals(riskAssessment.signals())).thenReturn("KNOWN_DEVICE");
        when(loginRiskService.deserializeSignals("KNOWN_DEVICE")).thenReturn(List.of("KNOWN_DEVICE"));

        when(userSessionRepository.findByUsuarioIdAndSistemaIdAndDeviceIdAndRevokedAtIsNull(userId, sistemaId, deviceId))
                .thenReturn(Optional.empty());
        when(userSessionRepository.findTopByUsuarioIdAndSistemaIdAndDeviceIdOrderByUpdatedAtDesc(userId, sistemaId, deviceId))
                .thenReturn(Optional.of(revokedSession));
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.generateToken(usuarioSistema, revokedSession)).thenReturn("access-token");
        when(jwtService.createRefreshToken(usuario, revokedSession)).thenReturn("refresh-token");

        LoginResponseDTO response = authService.confirmarEsqueciSenha(dto, context);

        assertEquals(deviceId, response.getDeviceId());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository).save(sessionCaptor.capture());
        assertEquals(sessionId, sessionCaptor.getValue().getId());
        assertNull(sessionCaptor.getValue().getRevokedAt());
    }
}

