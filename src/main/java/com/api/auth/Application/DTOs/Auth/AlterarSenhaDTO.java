package com.api.auth.Application.DTOs.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class AlterarSenhaDTO {
    @Schema(example = "SenhaAntiga@123")
    @NotBlank(message = "A senha atual é obrigatória")
    private String senhaAtual;

    @Schema(example = "NovaSenha@123")
    @NotBlank(message = "A nova senha é obrigatória")
    private String novaSenha;
}
