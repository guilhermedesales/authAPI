package com.api.auth.Application.DTOs.Auth.Login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LoginDTO {

    @NotBlank
    @Email(message = "Email inválido")
    private String email;
    @NotBlank
    private String senha;
    private UUID sistemaId;

}
