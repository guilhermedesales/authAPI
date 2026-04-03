package com.api.auth.Application.DTOs.Auth.EsqueciSenha;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EsqueciSenhaVerifyCodeDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String code;
}

