package com.api.auth.Application.DTOs.Auth.Login;

import com.api.auth.Domain.Enum.SessionTrustLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    @Schema(example = "false")
    private boolean stepUpRequired;

    @Schema(example = "c8f8fa11-a35f-4f12-a2bf-9db86bb6fd9f", nullable = true)
    private UUID challengeId;

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9...", nullable = true)
    private String accessToken;

    @Schema(example = "7fd7a42a-a741-4a26-b983-2e8b39c88943:5fa5ca29-a4ff-45f3-861f-7ff2cb7fd0cc", nullable = true)
    private String refreshToken;

    @Schema(example = "ef2fe5d4-5d1b-4f1a-8dd1-8529e7391ccb", nullable = true)
    private UUID deviceId;

    @Schema(example = "TRUSTED")
    private SessionTrustLevel sessionTrustLevel;

    @Schema(example = "35")
    private Integer riskScore;

    @Schema(example = "[\"NEW_DEVICE\",\"UNUSUAL_IP\"]")
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
