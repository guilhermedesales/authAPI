package com.api.auth.Application.DTOs.Auth.EsqueciSenha;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class EsqueciSenhaVerifyResponseDTO {
    private UUID challengeId;
}

