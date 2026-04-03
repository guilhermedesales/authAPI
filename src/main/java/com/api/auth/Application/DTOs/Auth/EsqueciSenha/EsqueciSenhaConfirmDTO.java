package com.api.auth.Application.DTOs.Auth.EsqueciSenha;

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

    @NotNull
    private UUID challengeId;

    @NotBlank
    private String novaSenha;

    @NotBlank
    private String confirmarNovaSenha;

    @NotNull
    private UUID sistemaId;
}

