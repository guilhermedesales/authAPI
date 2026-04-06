package com.api.auth.Application.DTOs.Auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class VerifyCodeDTO {
    @Schema(example = "123456")
    @NotBlank(message = "O código é obrigatório")
    @JsonAlias("codigo")
    private String code;

    @Schema(example = "c8f8fa11-a35f-4f12-a2bf-9db86bb6fd9f")
    @NotNull(message = "O challengeId é obrigatório")
    private UUID challengeId;

    @Schema(nullable = true, example = "ef2fe5d4-5d1b-4f1a-8dd1-8529e7391ccb")
    private UUID deviceId = null;
}
