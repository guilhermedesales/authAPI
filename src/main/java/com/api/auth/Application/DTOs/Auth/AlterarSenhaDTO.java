package com.api.auth.Application.DTOs.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class AlterarSenhaDTO {
    @NotBlank
    private String senhaAtual;
    @NotBlank
    private String novaSenha;
}
