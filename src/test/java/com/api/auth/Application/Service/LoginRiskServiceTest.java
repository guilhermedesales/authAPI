package com.api.auth.Application.Service;

import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Enum.SessionTrustLevel;
import com.api.auth.Infra.Repositories.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginRiskServiceTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    private LoginRiskService loginRiskService;

    @BeforeEach
    void setUp() {
        loginRiskService = new LoginRiskService(userSessionRepository);
        ReflectionTestUtils.setField(loginRiskService, "newDeviceScore", 40);
        ReflectionTestUtils.setField(loginRiskService, "newLocationScore", 35);
        ReflectionTestUtils.setField(loginRiskService, "unusualIpScore", 25);
        ReflectionTestUtils.setField(loginRiskService, "suspiciousThreshold", 40);
        ReflectionTestUtils.setField(loginRiskService, "stepUpThreshold", 60);
    }

    @Test
    void shouldReturnTrustedWhenNoBaselineSessionExists() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        Sistema sistema = new Sistema();
        sistema.setId(UUID.randomUUID());

        when(userSessionRepository.existsByUsuarioIdAndSistemaIdAndRevokedAtIsNull(usuario.getId(), sistema.getId()))
                .thenReturn(false);

        LoginRiskService.RiskAssessment result = loginRiskService.assess(
                usuario,
                sistema,
                UUID.randomUUID(),
                "203.0.113.10",
                "Sao Paulo, SP, BR"
        );

        assertEquals(0, result.score());
        assertEquals(SessionTrustLevel.TRUSTED, result.trustLevel());
        assertFalse(result.stepUpRequired());
        assertTrue(result.signals().isEmpty());
    }

    @Test
    void shouldRequireStepUpForHighRiskSignals() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        Sistema sistema = new Sistema();
        sistema.setId(UUID.randomUUID());

        UUID newDevice = UUID.randomUUID();

        when(userSessionRepository.existsByUsuarioIdAndSistemaIdAndRevokedAtIsNull(usuario.getId(), sistema.getId()))
                .thenReturn(true);
        when(userSessionRepository.findByUsuarioIdAndSistemaIdAndDeviceIdAndRevokedAtIsNull(usuario.getId(), sistema.getId(), newDevice))
                .thenReturn(Optional.empty());
        when(userSessionRepository.existsByUsuarioIdAndSistemaIdAndIpAndRevokedAtIsNull(usuario.getId(), sistema.getId(), "198.51.100.23"))
                .thenReturn(false);
        when(userSessionRepository.findDistinctActiveLocations(eq(usuario.getId()), eq(sistema.getId())))
                .thenReturn(List.of("Rio de Janeiro, RJ, BR"));

        LoginRiskService.RiskAssessment result = loginRiskService.assess(
                usuario,
                sistema,
                newDevice,
                "198.51.100.23",
                "Sao Paulo, SP, BR"
        );

        assertEquals(100, result.score());
        assertEquals(SessionTrustLevel.SUSPICIOUS, result.trustLevel());
        assertTrue(result.stepUpRequired());
        assertTrue(result.signals().contains("NEW_DEVICE"));
        assertTrue(result.signals().contains("UNUSUAL_IP"));
        assertTrue(result.signals().contains("NEW_LOCATION"));
    }
}


