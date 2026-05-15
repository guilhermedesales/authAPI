package com.api.auth.Application.Service;

import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Enum.SessionTrustLevel;
import com.api.auth.Infra.Repositories.UserSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoginRiskService {

    public record RiskAssessment(int score,
                                 SessionTrustLevel trustLevel,
                                 boolean stepUpRequired,
                                 List<String> signals) {
    }

    private static final String SIGNAL_NEW_DEVICE = "NEW_DEVICE";
    private static final String SIGNAL_NEW_LOCATION = "NEW_LOCATION";
    private static final String SIGNAL_UNUSUAL_IP = "UNUSUAL_IP";

    private final UserSessionRepository userSessionRepository;

    @Value("${auth.risk.new-device-score:40}")
    private int newDeviceScore;

    @Value("${auth.risk.new-location-score:35}")
    private int newLocationScore;

    @Value("${auth.risk.unusual-ip-score:25}")
    private int unusualIpScore;

    @Value("${auth.risk.suspicious-threshold:40}")
    private int suspiciousThreshold;

    @Value("${auth.risk.step-up-threshold:60}")
    private int stepUpThreshold;

    public LoginRiskService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    public RiskAssessment assess(Usuario usuario, Sistema sistema, UUID deviceId, String ip, String location) {
        UUID usuarioId = usuario.getId();
        UUID sistemaId = sistema.getId();

        boolean hasBaseline = userSessionRepository.existsByUsuarioIdAndSistemaIdAndRevokedAtIsNull(usuarioId, sistemaId);
        if (!hasBaseline) {
            return new RiskAssessment(0, SessionTrustLevel.TRUSTED, false, List.of());
        }

        int score = 0;
        List<String> signals = new ArrayList<>();

        boolean knownDevice = deviceId != null && userSessionRepository
                .findByUsuarioIdAndSistemaIdAndDeviceIdAndRevokedAtIsNull(usuarioId, sistemaId, deviceId)
                .isPresent();

        if (!knownDevice) {
            score += newDeviceScore;
            signals.add(SIGNAL_NEW_DEVICE);
        }

        if (hasValue(ip) && !userSessionRepository.existsByUsuarioIdAndSistemaIdAndIpAndRevokedAtIsNull(usuarioId, sistemaId, ip)) {
            score += unusualIpScore;
            signals.add(SIGNAL_UNUSUAL_IP);
        }

        if (hasMeaningfulLocation(location) && isNewLocation(usuarioId, sistemaId, location)) {
            score += newLocationScore;
            signals.add(SIGNAL_NEW_LOCATION);
        }

        SessionTrustLevel trustLevel = score >= suspiciousThreshold
                ? SessionTrustLevel.SUSPICIOUS
                : SessionTrustLevel.TRUSTED;

        return new RiskAssessment(score, trustLevel, score >= stepUpThreshold, List.copyOf(signals));
    }

    public String serializeSignals(List<String> signals) {
        if (signals == null || signals.isEmpty()) {
            return null;
        }
        return String.join(",", signals);
    }

    public List<String> deserializeSignals(String signals) {
        if (!hasValue(signals)) {
            return List.of();
        }
        return List.of(signals.split(","));
    }

    private boolean isNewLocation(UUID usuarioId, UUID sistemaId, String location) {
        Set<String> known = userSessionRepository.findDistinctActiveLocations(usuarioId, sistemaId)
                .stream()
                .filter(this::hasMeaningfulLocation)
                .map(this::normalize)
                .collect(Collectors.toSet());

        if (known.isEmpty()) {
            return false;
        }

        return !known.contains(normalize(location));
    }

    private boolean hasMeaningfulLocation(String location) {
        if (!hasValue(location)) {
            return false;
        }
        String normalized = normalize(location);
        return !"unknown".equals(normalized) && !"localhost".equals(normalized);
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}


