package com.api.auth.Application.DTOs.Auth.Login;

import com.api.auth.Domain.Enum.SessionTrustLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private boolean stepUpRequired;
    private UUID challengeId;
    private String accessToken;
    private String refreshToken;
    private UUID deviceId;
    private SessionTrustLevel sessionTrustLevel;
    private Integer riskScore;
    private List<String> riskSignals;

    public static LoginResponseDTO authenticated(String accessToken,
                                                 String refreshToken,
                                                 UUID deviceId,
                                                 SessionTrustLevel sessionTrustLevel,
                                                 Integer riskScore,
                                                 List<String> riskSignals) {
        return new LoginResponseDTO(false, null, accessToken, refreshToken, deviceId, sessionTrustLevel, riskScore, riskSignals);
    }

    public static LoginResponseDTO stepUpRequired(UUID challengeId, Integer riskScore, List<String> riskSignals) {
        return new LoginResponseDTO(true, challengeId, null, null, null, SessionTrustLevel.SUSPICIOUS, riskScore, riskSignals);
    }
}
