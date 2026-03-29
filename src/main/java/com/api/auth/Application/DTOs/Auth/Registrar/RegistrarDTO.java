package com.api.auth.Application.DTOs.Auth.Registrar;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistrarDTO {

    @NotBlank
    private String nome;
    @Email(message = "Email inválido")
    @NotBlank
    private String email;
    private String senha;

}

