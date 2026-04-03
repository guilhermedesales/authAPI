package com.api.auth.Application.DTOs.Auth.EsqueciSenha;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EsqueciSenhaDTO {
    @NotBlank
    @Email
    private String email;
}
