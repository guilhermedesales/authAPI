package com.api.auth.Application.DTOs.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class VerifyCodeDTO {
    @NotBlank
    private String code;

    @NotNull
    private UUID challengeId;

    @Schema(nullable = true, example = "")
    private UUID deviceId = null;
}
