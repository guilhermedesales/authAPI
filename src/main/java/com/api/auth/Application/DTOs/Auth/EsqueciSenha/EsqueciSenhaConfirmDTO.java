package com.api.auth.Application.DTOs.Auth.EsqueciSenha;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EsqueciSenhaConfirmDTO {

    @Schema(example = "c8f8fa11-a35f-4f12-a2bf-9db86bb6fd9f")
    @NotNull(message = "O challengeId é obrigatório")
    private UUID challengeId;

    @Schema(example = "NovaSenha@123")
    @NotBlank(message = "A nova senha é obrigatória")
    private String novaSenha;

    @Schema(example = "NovaSenha@123")
    @NotBlank(message = "A confirmação da nova senha é obrigatória")
    private String confirmarNovaSenha;

    @Schema(example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    @NotNull(message = "O sistemaId é obrigatório")
    private UUID sistemaId;

    @Schema(example = "d290f1ee-6c54-4b01-90e6-d701748f0851", nullable = true)
    private UUID deviceId;
}

