package com.api.auth.Application.DTOs.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class OtpChallengeResponseDTO {
    @Schema(example = "c8f8fa11-a35f-4f12-a2bf-9db86bb6fd9f")
    private UUID challengeId;
}

